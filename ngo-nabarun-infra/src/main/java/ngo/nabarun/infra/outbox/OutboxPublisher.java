package ngo.nabarun.infra.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ngo.nabarun.common.event.CustomEvent;
import ngo.nabarun.common.event.CustomEventPublisher;
import ngo.nabarun.infra.mongo.entity.OutboxEventEntity;
import ngo.nabarun.infra.mongo.repo.OutboxEventRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxPublisher implements CustomEventPublisher{

    private final OutboxEventRepository repo;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;


    public OutboxPublisher(OutboxEventRepository repo, ObjectMapper objectMapper,ApplicationEventPublisher eventPublisher) {
        this.repo = repo;
        this.objectMapper = objectMapper;
        this.eventPublisher=eventPublisher;
    }

    /**
     * Persist the domain event into outbox collection.
     * Must be called inside the same transactional boundary as domain changes.
     */
    @Transactional
    @Override
    public void publishEvent(CustomEvent domainEvent) {
        try {
            OutboxEventEntity e = new OutboxEventEntity();
            e.setEventType(domainEvent.getClass().getName());
            e.setPayload(objectMapper.writeValueAsString(domainEvent));
            e.setStatus(ngo.nabarun.infra.outbox.OutboxStatus.PENDING);
            e.setCreatedAt(new java.util.Date());
            repo.save(e);
            eventPublisher.publishEvent(new OutboxSavedEvent(e.getId()));
            System.out.println("Event Saved and published. id->"+e.getId());
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Failed to serialize domain event", ex);
        }
    }
}
