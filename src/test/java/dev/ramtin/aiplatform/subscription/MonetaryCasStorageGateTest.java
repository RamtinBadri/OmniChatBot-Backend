package dev.ramtin.aiplatform.subscription;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.result.UpdateResult;
import dev.ramtin.aiplatform.user.user_balance.UserBalance;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Storage-level gate, not service regression coverage. No application context or configured database.
 * Run explicitly with -Dstorage.cas.mongo=true against local MongoDB.
 */
@EnabledIfSystemProperty(named = "storage.cas.mongo", matches = "true")
class MonetaryCasStorageGateTest {
    private String databaseName;
    private MongoClient client;
    private MongoTemplate template;
    private boolean created;

    @BeforeEach
    void createDisposableDatabase() {
        databaseName = "storage_cas_fixture_" + UUID.randomUUID().toString().replace("-", "");
        client = MongoClients.create("mongodb://localhost:27017/" + databaseName
                + "?serverSelectionTimeoutMS=3000&connectTimeoutMS=3000&socketTimeoutMS=5000"
                + "&retryWrites=false&retryReads=false");
        template = new MongoTemplate(client, databaseName);
        // Only a successful create grants this fixture permission to drop the generated database.
        template.createCollection("fixture_ownership");
        created = true;
        System.out.println("Created disposable MongoDB fixture: " + databaseName);
    }

    @AfterEach
    void dropOnlyCreatedDatabase() {
        try {
            if (created) {
                assertTrue(databaseName.matches("storage_cas_fixture_[0-9a-f]{32}"));
                client.getDatabase(databaseName).drop();
                System.out.println("Dropped disposable MongoDB fixture: " + databaseName);
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Test
    void mapsDecimal128AmountsAndObjectIdsForExactCasIncludingScales() {
        for (String value : new String[]{"0.00", "12.3400", "0.00000001", "12345678901234567890.12345678901234"}) {
            UserBalance balance = template.insert(UserBalance.builder()
                    .userId(UUID.randomUUID().toString())
                    .balance(new BigDecimal(value)).currency("USD").build());
            Document stored = template.getCollection("user_balances")
                    .find(new Document("_id", new org.bson.types.ObjectId(balance.getId()))).first();
            assertNotNull(stored);
            assertInstanceOf(Decimal128.class, stored.get("balance"));
            assertEquals(new BigDecimal(value), ((Decimal128) stored.get("balance")).bigDecimalValue());
            UserBalance observed = template.findById(balance.getId(), UserBalance.class);
            assertNotNull(observed);
            BigDecimal replacement = observed.getBalance().subtract(new BigDecimal("0.00000001"));
            assertEquals(1, balanceCas(observed, replacement).getMatchedCount());
            assertEquals(0, balanceCas(observed, BigDecimal.TEN).getMatchedCount());
            assertEquals(replacement, template.findById(balance.getId(), UserBalance.class).getBalance());

            Subscription subscription = template.insert(Subscription.builder()
                    .userId(UUID.randomUUID().toString()).status(SubscriptionStatus.ACTIVE)
                    .endDate(LocalDateTime.now().plusDays(1)).remainingCredit(new BigDecimal(value)).build());
            Document storedSubscription = template.getCollection("subscriptions")
                    .find(new Document("_id", new org.bson.types.ObjectId(subscription.getId()))).first();
            assertNotNull(storedSubscription);
            assertInstanceOf(Decimal128.class, storedSubscription.get("remainingCredit"));
            assertEquals(new BigDecimal(value),
                    ((Decimal128) storedSubscription.get("remainingCredit")).bigDecimalValue());
            Subscription observedSubscription = template.findById(subscription.getId(), Subscription.class);
            assertNotNull(observedSubscription);
            assertEquals(1, subscriptionCas(observedSubscription, replacement).getMatchedCount());
            assertEquals(0, subscriptionCas(observedSubscription, BigDecimal.TEN).getMatchedCount());
            assertEquals(replacement, template.findById(subscription.getId(), Subscription.class).getRemainingCredit());
        }
    }

    @Test
    void readableStringAmountsDoNotMatchDefaultMappedDecimalCas() {
        // A legacy/external representation, seeded here only; no existing application data is read.
        template.getCollection("user_balances").insertOne(new Document("_id", "legacy-balance")
                .append("userId", "legacy-user").append("balance", "12.3400"));
        UserBalance balance = template.findById("legacy-balance", UserBalance.class);
        assertNotNull(balance);
        assertEquals(new BigDecimal("12.3400"), balance.getBalance());
        assertEquals(0, balanceCas(balance, new BigDecimal("11.3400")).getMatchedCount());
        assertEquals("12.3400", template.getCollection("user_balances")
                .find(new Document("_id", "legacy-balance")).first().get("balance"));

        template.getCollection("subscriptions").insertOne(new Document("_id", "legacy-subscription")
                .append("userId", "legacy-user").append("status", "ACTIVE")
                .append("remainingCredit", "12.3400").append("endDate", new java.util.Date(4102444800000L)));
        Subscription subscription = template.findById("legacy-subscription", Subscription.class);
        assertNotNull(subscription);
        assertEquals(new BigDecimal("12.3400"), subscription.getRemainingCredit());
        assertEquals(0, subscriptionCas(subscription, new BigDecimal("11.3400")).getMatchedCount());
        assertEquals("12.3400", template.getCollection("subscriptions")
                .find(new Document("_id", "legacy-subscription")).first().get("remainingCredit"));
    }

    @Test
    void concurrentTopUpAndDebitCollideThenRecomputeWithoutLostUpdate() throws Exception {
        UserBalance initial = template.insert(UserBalance.builder()
                .userId("balance-user").balance(new BigDecimal("10.0000")).currency("USD").build());
        CyclicBarrier sameSnapshot = new CyclicBarrier(2);
        int[] collisions = race(
                () -> changeBalance(initial.getId(), new BigDecimal("5.2500"), sameSnapshot),
                () -> changeBalance(initial.getId(), new BigDecimal("-4.1250"), sameSnapshot));
        assertEquals(1, collisions[0] + collisions[1]);
        assertEquals(new BigDecimal("11.1250"), template.findById(initial.getId(), UserBalance.class).getBalance());
    }

    @Test
    void concurrentSubscriptionDebitsCollideThenRecomputeWithoutLostUpdate() throws Exception {
        Subscription initial = insertSubscription(LocalDateTime.now().plusDays(1));
        CyclicBarrier sameSnapshot = new CyclicBarrier(2);
        int[] collisions = race(
                () -> debitSubscription(initial.getId(), new BigDecimal("3.1250"), sameSnapshot),
                () -> debitSubscription(initial.getId(), new BigDecimal("2.2500"), sameSnapshot));
        assertEquals(1, collisions[0] + collisions[1]);
        assertEquals(new BigDecimal("4.6250"), template.findById(initial.getId(), Subscription.class).getRemainingCredit());
    }

    @Test
    void cancellationAndDebitRaceCannotRestoreCreditOrDebitAfterCancellation() throws Exception {
        Subscription initial = insertSubscription(LocalDateTime.now().plusDays(1));
        Subscription stale = template.findById(initial.getId(), Subscription.class);
        CyclicBarrier start = new CyclicBarrier(2);
        int[] results = race(
                () -> {
                    start.await(5, TimeUnit.SECONDS);
                    return (int) subscriptionCas(stale, new BigDecimal("7.0000")).getMatchedCount();
                },
                () -> {
                    start.await(5, TimeUnit.SECONDS);
                    return (int) template.updateFirst(Query.query(Criteria.where("id").is(stale.getId())),
                            Update.update("status", SubscriptionStatus.CANCELLED), Subscription.class).getMatchedCount();
                });
        assertEquals(1, results[1]);
        Subscription stored = template.findById(initial.getId(), Subscription.class);
        assertEquals(SubscriptionStatus.CANCELLED, stored.getStatus());
        assertEquals(results[0] == 1 ? new BigDecimal("7.0000") : new BigDecimal("10.0000"), stored.getRemainingCredit());
        assertEquals(0, subscriptionCas(stale, BigDecimal.ONE).getMatchedCount());
    }

    @Test
    void conditionalExpiryRacingCancellationNeverOverwritesCancellationOrCredit() throws Exception {
        Subscription initial = insertSubscription(LocalDateTime.now().minusDays(1));
        Subscription stale = template.findById(initial.getId(), Subscription.class);
        CyclicBarrier start = new CyclicBarrier(2);
        race(
                () -> {
                    start.await(5, TimeUnit.SECONDS);
                    return (int) expire(stale).getMatchedCount();
                },
                () -> {
                    start.await(5, TimeUnit.SECONDS);
                    return (int) template.updateFirst(Query.query(Criteria.where("id").is(stale.getId())),
                            Update.update("status", SubscriptionStatus.CANCELLED), Subscription.class).getMatchedCount();
                });
        Subscription stored = template.findById(initial.getId(), Subscription.class);
        assertEquals(SubscriptionStatus.CANCELLED, stored.getStatus());
        assertEquals(new BigDecimal("10.0000"), stored.getRemainingCredit());
        assertEquals(0, expire(stale).getMatchedCount());
    }

    @Test
    void observedEndDateGuardsBothDebitAndExpiryAndStatusWritesPreserveChangedCredit() {
        Subscription initial = insertSubscription(LocalDateTime.now().minusDays(2));
        Subscription stale = template.findById(initial.getId(), Subscription.class);
        template.updateFirst(Query.query(Criteria.where("id").is(initial.getId())),
                Update.update("endDate", initial.getEndDate().plusDays(1)), Subscription.class);
        assertEquals(0, subscriptionCas(stale, BigDecimal.ONE).getMatchedCount());
        assertEquals(0, expire(stale).getMatchedCount());
        Subscription current = template.findById(initial.getId(), Subscription.class);
        assertEquals(1, subscriptionCas(current, new BigDecimal("6.1250")).getMatchedCount());
        assertEquals(1, expire(current).getMatchedCount());
        Subscription stored = template.findById(initial.getId(), Subscription.class);
        assertEquals(SubscriptionStatus.EXPIRED, stored.getStatus());
        assertEquals(new BigDecimal("6.1250"), stored.getRemainingCredit());
    }

    private Subscription insertSubscription(LocalDateTime endDate) {
        return template.insert(Subscription.builder().userId("subscription-user")
                .status(SubscriptionStatus.ACTIVE).endDate(endDate)
                .remainingCredit(new BigDecimal("10.0000")).build());
    }

    private UpdateResult balanceCas(UserBalance observed, BigDecimal replacement) {
        return template.updateFirst(Query.query(Criteria.where("id").is(observed.getId())
                        .and("balance").is(observed.getBalance())),
                Update.update("balance", replacement).set("updatedAt", LocalDateTime.now()), UserBalance.class);
    }

    private Query activeSnapshot(Subscription observed) {
        return Query.query(Criteria.where("id").is(observed.getId())
                .and("status").is(SubscriptionStatus.ACTIVE).and("endDate").is(observed.getEndDate()));
    }

    private UpdateResult subscriptionCas(Subscription observed, BigDecimal replacement) {
        Query query = activeSnapshot(observed);
        query.addCriteria(Criteria.where("remainingCredit").is(observed.getRemainingCredit()));
        return template.updateFirst(query, Update.update("remainingCredit", replacement), Subscription.class);
    }

    private UpdateResult expire(Subscription observed) {
        return template.updateFirst(activeSnapshot(observed),
                Update.update("status", SubscriptionStatus.EXPIRED), Subscription.class);
    }

    private int changeBalance(String id, BigDecimal delta, CyclicBarrier sameSnapshot) throws Exception {
        for (int attempt = 0; attempt < 5; attempt++) {
            UserBalance observed = template.findById(id, UserBalance.class);
            if (attempt == 0) {
                sameSnapshot.await(5, TimeUnit.SECONDS);
            }
            UpdateResult result = balanceCas(observed, observed.getBalance().add(delta));
            assertTrue(result.wasAcknowledged());
            if (result.getMatchedCount() == 1) {
                return attempt;
            }
            assertEquals(0, result.getMatchedCount());
        }
        throw new AssertionError("Fixture CAS exceeded five attempts");
    }

    private int debitSubscription(String id, BigDecimal amount, CyclicBarrier sameSnapshot) throws Exception {
        for (int attempt = 0; attempt < 5; attempt++) {
            Subscription observed = template.findById(id, Subscription.class);
            if (attempt == 0) {
                sameSnapshot.await(5, TimeUnit.SECONDS);
            }
            UpdateResult result = subscriptionCas(observed, observed.getRemainingCredit().subtract(amount));
            assertTrue(result.wasAcknowledged());
            if (result.getMatchedCount() == 1) {
                return attempt;
            }
            assertEquals(0, result.getMatchedCount());
        }
        throw new AssertionError("Fixture CAS exceeded five attempts");
    }

    private int[] race(Callable<Integer> first, Callable<Integer> second) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> left = pool.submit(first);
            Future<Integer> right = pool.submit(second);
            return new int[]{left.get(15, TimeUnit.SECONDS), right.get(15, TimeUnit.SECONDS)};
        } finally {
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        }
    }
}
