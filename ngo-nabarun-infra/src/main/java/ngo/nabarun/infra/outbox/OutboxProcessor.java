/*
 * package ngo.nabarun.infra.outbox;
 * 
 * import java.util.List;
 * 
 * import org.springframework.scheduling.annotation.Async; import
 * org.springframework.stereotype.Component; import
 * org.springframework.transaction.event.TransactionPhase; import
 * org.springframework.transaction.event.TransactionalEventListener;
 * 
 * import ngo.nabarun.infra.mongo.entity.OutboxEventEntity; import
 * ngo.nabarun.infra.mongo.repo.OutboxEventRepository;
 * 
 * 
 * @Component public class OutboxProcessor {
 * 
 * private final OutboxEventRepository outboxRepository; private final
 * CustomEventDispatcher dispatcher;
 * 
 * public OutboxProcessor(OutboxEventRepository outboxRepository,
 * CustomEventDispatcher dispatcher) { this.outboxRepository = outboxRepository;
 * this.dispatcher = dispatcher; }
 * 
 *//** Immediate async processing for a newly saved event */
/*
 * @Async
 * 
 * @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) public
 * void handleOutboxSaved(OutboxCreatedEvent event) {
 * System.out.println("Event Listened. id->"+event.outboxId());
 * processById(event.outboxId()); }
 * 
 *//** Opportunistic retry: process pending events on incoming request */
/*
 * @Async public void retryPendingEvents(int maxProcess) {
 * List<OutboxEventEntity> pending = outboxRepository
 * .findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING); int processed = 0;
 * for (OutboxEventEntity event : pending) { if (processed >= maxProcess) break;
 * processById(event.getId()); processed++; } }
 * 
 *//** Internal method to process safely */
/*
 * public void processById(String outboxId) {
 * outboxRepository.findById(outboxId).ifPresent(this::processEventSafely); }
 * 
 *//** Actual processing with retry logic and status update *//*
																 * private void processEventSafely(OutboxEventEntity
																 * outboxEvent) { try {
																 * System.out.println("Processing Event. id->"
																 * +outboxEvent.getId());
																 * 
																 * dispatcher.dispatch(outboxEvent.getEventType(),
																 * outboxEvent.getPayload());
																 * 
																 * outboxEvent.setStatus(OutboxStatus.SUCCESS);
																 * outboxEvent.setProcessedAt(new java.util.Date());
																 * outboxEvent.setErrorMessage(null);
																 * outboxRepository.save(outboxEvent); } catch
																 * (Exception e) { System.out.
																 * println("Processing Failed for Event. id->"
																 * +outboxEvent.getId());
																 * outboxEvent.setRetryCount(outboxEvent.getRetryCount()
																 * + 1); outboxEvent.setLastTriedAt(new
																 * java.util.Date());
																 * outboxEvent.setErrorMessage(e.getMessage()); if
																 * (outboxEvent.getRetryCount() >=
																 * outboxEvent.getMaxAttempts()) {
																 * outboxEvent.setStatus(OutboxStatus.FAILED); } else {
																 * outboxEvent.setStatus(OutboxStatus.PENDING); }
																 * outboxRepository.save(outboxEvent); } } }
																 */