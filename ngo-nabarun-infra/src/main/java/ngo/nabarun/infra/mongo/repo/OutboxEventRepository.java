package ngo.nabarun.infra.mongo.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ngo.nabarun.infra.mongo.entity.OutboxEventEntity;
import ngo.nabarun.outbox.domain.enums.OutboxStatus;

import java.util.List;

@Repository
public interface OutboxEventRepository extends MongoRepository<OutboxEventEntity, String> {
	List<OutboxEventEntity> findByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
