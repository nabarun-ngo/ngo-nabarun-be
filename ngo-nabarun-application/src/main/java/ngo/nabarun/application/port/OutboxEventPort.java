package ngo.nabarun.application.port;

import java.util.List;

import ngo.nabarun.application.dto.result.OutboxEventResult;

public interface OutboxEventPort {
	
	void retryEvent(String id);
	void retryPendingEvents();
	List<OutboxEventResult> fetchOutboxEvents();
	List<OutboxEventResult> fetchDeliveredEvents();

}
