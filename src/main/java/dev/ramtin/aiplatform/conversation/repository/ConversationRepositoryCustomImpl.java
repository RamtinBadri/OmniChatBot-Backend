package dev.ramtin.aiplatform.conversation.repository;

import dev.ramtin.aiplatform.conversation.Conversation;
import dev.ramtin.aiplatform.conversation.ConversationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@RequiredArgsConstructor
public class ConversationRepositoryCustomImpl implements ConversationRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Conversation> search(String userId, String search, ConversationStatus status, Pageable pageable) {
        Criteria criteria = Criteria.where("userId").is(userId);

        if (search != null && !search.isBlank()) {
            criteria.and("title").regex(search, "i");
        }

        if (status != null) {
            criteria.and("status").is(status);
        }

        Query query = new Query(criteria).with(pageable);
        List<Conversation> conversations = mongoTemplate.find(query, Conversation.class);
        long total = mongoTemplate.count(Query.query(criteria), Conversation.class);

        return new PageImpl<>(conversations, pageable, total);
    }
}