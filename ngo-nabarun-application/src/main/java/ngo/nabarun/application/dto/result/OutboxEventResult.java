package ngo.nabarun.application.dto.result;

import java.util.Date;

import lombok.Data;
import ngo.nabarun.outbox.domain.enums.OutboxStatus;

@Data
public class OutboxEventResult {
	private final String id;
	private final String eventType;
	private final String payload;
	private OutboxStatus status;
	private final Date createdAt;
	private final int maxAttempts;
	private int retryCount;
	private Date processFailedAt;
	private Date processStartAt;
	private Date processEndAt;
	private String errorMessage;
}
