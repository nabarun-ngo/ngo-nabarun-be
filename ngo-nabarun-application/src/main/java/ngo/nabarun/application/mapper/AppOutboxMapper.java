package ngo.nabarun.application.mapper;

import org.mapstruct.Mapper;

import ngo.nabarun.application.dto.result.OutboxEventResult;
import ngo.nabarun.outbox.domain.EventOutbox;

@Mapper(componentModel = "spring")
public interface AppOutboxMapper {

	OutboxEventResult toResult(EventOutbox domain);

}
