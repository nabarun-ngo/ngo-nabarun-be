package ngo.nabarun.application.service;

import java.util.List;

import ngo.nabarun.application.dto.result.OutboxEventResult;
import ngo.nabarun.outbox.domain.enums.OutboxStatus;

public interface AdminService {
	
	List<OutboxEventResult> getOutboxEvents(OutboxStatus status);

	OutboxEventResult getOutboxEvent(String id);

	void processFromOutbox(String id);

	void retryPendingEvents(int limit);

}
