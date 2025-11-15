package commerce.eshop.core.application.infrastructure.impl;

import commerce.eshop.core.application.events.auditing_events.AuditingImmediateEvent;
import commerce.eshop.core.application.events.auditing_events.AuditingMethodEvent;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.application.infrastructure.AuditingService;
import jakarta.annotation.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;


import java.util.UUID;

@Component
public class AuditingServiceImpl implements AuditingService {

    private final ApplicationEventPublisher publisher;

    public AuditingServiceImpl(ApplicationEventPublisher publisher){
        this.publisher = publisher;
    }

    @Override
    public void log(@Nullable UUID customerId, String methodName, AuditingStatus status, String reason) {
        if (status == AuditingStatus.ERROR){
            publisher.publishEvent(new AuditingImmediateEvent(customerId, methodName, status, reason));
        } else {
            publisher.publishEvent(new AuditingMethodEvent(customerId, methodName, status, reason));
        }
    }
}
