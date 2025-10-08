package ngo.nabarun.infra.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ngo.nabarun.infra.mapper.InfraOutboxMapper;
import ngo.nabarun.infra.mongo.repo.OutboxEventRepository;
import ngo.nabarun.outbox.domain.EventOutbox;
import ngo.nabarun.outbox.domain.enums.OutboxStatus;
import ngo.nabarun.outbox.domain.port.EventOutboxRepositoryPort;

@Service
public class OutboxEventAdapter implements EventOutboxRepositoryPort {

	@Autowired
	private OutboxEventRepository outboxRepo;

	@Autowired
	private InfraOutboxMapper outboxMapper;

	@Override
	public Optional<EventOutbox> findById(String id) {
		return outboxRepo.findById(id).map(outboxMapper::toEventOutbox);
	}

	@Override
	public EventOutbox save(EventOutbox event) {
		var entity = outboxMapper.toOutboxEventEntity(event);
		entity = outboxRepo.save(entity);
		return outboxMapper.toEventOutbox(entity);
	}

	@Override
	@Deprecated
	public void updateStatus(String id, OutboxStatus status) {
		
	}

	@Override
	public List<EventOutbox> findByStatusOrderByCreatedAtAsc(OutboxStatus pending) {
		return outboxRepo.findByStatusOrderByCreatedAtAsc(pending).stream().map(outboxMapper::toEventOutbox).toList();
	}

}
