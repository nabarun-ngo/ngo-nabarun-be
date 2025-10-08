package ngo.nabarun.infra.outbox;

import ngo.nabarun.infra.mongo.entity.OutboxEventEntity;

public interface OutboxEventRepositoryCustom {
    OutboxEventEntity claimNextPendingEvent(java.time.Duration processingTimeout);
}
