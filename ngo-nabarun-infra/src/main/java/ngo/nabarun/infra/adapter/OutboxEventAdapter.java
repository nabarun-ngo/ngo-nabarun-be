package ngo.nabarun.infra.adapter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ngo.nabarun.application.dto.result.OutboxEventResult;
import ngo.nabarun.application.port.OutboxEventPort;
import ngo.nabarun.infra.mongo.repo.OutboxEventRepository;
import ngo.nabarun.infra.outbox.OutboxProcessor;
import ngo.nabarun.infra.outbox.OutboxStatus;


@Service
public class OutboxEventAdapter implements OutboxEventPort{
	
	@Autowired private OutboxProcessor outboxProcessor;
	
	@Autowired private OutboxEventRepository obEventRepo;

	@Override
	public void retryEvent(String id) {
		outboxProcessor.processById(id);
	}

	@Override
	public void retryPendingEvents() {
		outboxProcessor.retryPendingEvents(10);		
	}

	@Override
	public List<OutboxEventResult> fetchOutboxEvents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OutboxEventResult> fetchDeliveredEvents() {
		// TODO Auto-generated method stub
		return null;
	}


	
	

}
