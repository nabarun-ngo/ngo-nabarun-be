package ngo.nabarun.application.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ngo.nabarun.application.dto.result.OutboxEventResult;
import ngo.nabarun.application.mapper.AppOutboxMapper;
import ngo.nabarun.application.service.AdminService;
import ngo.nabarun.outbox.domain.enums.OutboxStatus;
import ngo.nabarun.outbox.domain.port.EventOutboxRepositoryPort;
import ngo.nabarun.outbox.service.OutboxProcessor;

@Service
public class AdminServiceImpl implements AdminService {

	@Autowired
	private EventOutboxRepositoryPort eventRepo;

	@Autowired
	private OutboxProcessor obProcessor;
	
	@Autowired
	private AppOutboxMapper outboxMapper;

	@Override
	public List<OutboxEventResult> getOutboxEvents(OutboxStatus status) {
		return eventRepo.findByStatusOrderByCreatedAtAsc(status).stream().map(outboxMapper::toResult).toList();
	}
	
	@Override
	public OutboxEventResult getOutboxEvent(String id) {
		var item = eventRepo.findById(id).orElseThrow(()-> new RuntimeException("Item not found"));
		return outboxMapper.toResult(item);
	}
	
	@Override
	public void processFromOutbox(String id) {
		obProcessor.processById(id);;
	}
	
	@Override
	public void retryPendingEvents(int limit) {
		obProcessor.retryPendingEvents(limit);
	}

}
