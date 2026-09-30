package dev.ramtin.aiplatform.user.repository;

import dev.ramtin.aiplatform.user.User;
import dev.ramtin.aiplatform.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    @Override
    public Page<User> search(String search, String role, UserStatus status, Pageable pageable) {
        Query query = new Query();

        if (search != null && !search.isBlank()) {
            Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("email").regex(search, "i"),
                    Criteria.where("username").regex(search, "i"),
                    Criteria.where("firstName").regex(search, "i"),
                    Criteria.where("lastName").regex(search, "i")
            );
            query.addCriteria(searchCriteria);
        }

        if (role != null && !role.isBlank()) {
            query.addCriteria(Criteria.where("userRole").is(role));
        }

        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }

        long total = mongoTemplate.count(query, User.class);
        query.with(pageable);
        List<User> users = mongoTemplate.find(query, User.class);
        return new PageImpl<>(users, pageable, total);
    }
}
