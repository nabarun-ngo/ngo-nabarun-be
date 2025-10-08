package ngo.nabarun.infra.outbox;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.concurrent.CompletableFuture;

@Component
public class OutboxRetryInterceptor implements HandlerInterceptor {

    private final OutboxProcessor outboxProcessor;

    public OutboxRetryInterceptor(OutboxProcessor outboxProcessor) {
        this.outboxProcessor = outboxProcessor;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // fire-and-forget; do not block request
        CompletableFuture.runAsync(() -> {
            try {
                outboxProcessor.retryPendingEvents(5);
            } catch (Exception e) {
                // log and ignore — do not fail user request
            }
        });
        return true;
    }
}
