package dev.ramtin.aiplatform.conversation;

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
    public Page<Conversation> search(String userId, String search, Boolean archived, Pageable pageable) {
        Criteria criteria = Criteria.where("userId").is(userId);

        if (search != null && !search.isBlank()) {
            criteria.and("title").regex(search, "i");
        }

        if (archived != null) {
            criteria.and("archived").is(archived);
        }

        Query query = new Query(criteria).with(pageable);
        List<Conversation> conversations = mongoTemplate.find(query, Conversation.class);
        long total = mongoTemplate.count(Query.query(criteria), Conversation.class);

        return new PageImpl<>(conversations, pageable, total);
    }
}