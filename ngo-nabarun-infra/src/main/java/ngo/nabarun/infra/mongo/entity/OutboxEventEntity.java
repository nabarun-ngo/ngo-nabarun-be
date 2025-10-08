package ngo.nabarun.infra.mongo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import ngo.nabarun.infra.outbox.OutboxStatus;

import java.util.Date;

@Document(collection = "outbox_events")
@Data
public class OutboxEventEntity {
    @Id
    private String id;
    private String eventType;    // fully-qualified class name
    private String payload;      // JSON
    private OutboxStatus status = OutboxStatus.PENDING;
    private int retryCount = 0;
    private int maxAttempts = 5;
	@Indexed(name = "createdAt", expireAfter = "10d")
    private Date createdAt = new Date();
    private Date lastTriedAt;
    private Date processedAt;
    private String errorMessage;

}
