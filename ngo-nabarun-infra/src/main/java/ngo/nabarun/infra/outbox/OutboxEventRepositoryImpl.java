package ngo.nabarun.infra.outbox;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import ngo.nabarun.infra.mongo.entity.OutboxEventEntity;

import java.time.Instant;
import java.util.Date;

@Repository
public class OutboxEventRepositoryImpl implements OutboxEventRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public OutboxEventRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public OutboxEventEntity claimNextPendingEvent(java.time.Duration processingTimeout) {
        Date stale = Date.from(Instant.now().minus(processingTimeout));
        Query q = new Query(new Criteria().orOperator(
                Criteria.where("status").is(OutboxStatus.PENDING),
                new Criteria().andOperator(
                        Criteria.where("status").is(OutboxStatus.PROCESSING),
                        Criteria.where("lastTriedAt").lte(stale)
                )
        )).with(Sort.by(Sort.Direction.ASC, "createdAt")).limit(1);

        Update u = new Update()
                .set("status", OutboxStatus.PROCESSING)
                .set("lastTriedAt", new Date())
                .inc("retryCount", 1);

        FindAndModifyOptions opts = new FindAndModifyOptions().returnNew(true);
        return mongoTemplate.findAndModify(q, u, opts, OutboxEventEntity.class);
    }
}
