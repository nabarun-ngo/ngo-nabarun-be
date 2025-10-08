package ngo.nabarun.infra.mapper;

import org.mapstruct.Mapper;

import ngo.nabarun.infra.mongo.entity.OutboxEventEntity;
import ngo.nabarun.outbox.domain.EventOutbox;

@Mapper(componentModel = "spring")
public interface InfraOutboxMapper {
	
	EventOutbox toEventOutbox(OutboxEventEntity entity);
	OutboxEventEntity toOutboxEventEntity(EventOutbox outbox);

}
