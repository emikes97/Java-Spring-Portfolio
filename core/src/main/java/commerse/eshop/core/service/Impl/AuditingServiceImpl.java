package commerse.eshop.core.service.Impl;

import commerse.eshop.core.events.auditing_events.AuditingImmediateEvent;
import commerse.eshop.core.events.auditing_events.AuditingMethodEvent;
import commerse.eshop.core.model.entity.enums.AuditingStatus;
import commerse.eshop.core.service.AuditingService;
import org.springframework.context.ApplicationEventPublisher;


import java.util.UUID;

public class AuditingServiceImpl implements AuditingService {

    private final ApplicationEventPublisher publisher;

    public AuditingServiceImpl(ApplicationEventPublisher publisher){
        this.publisher = publisher;
    }

    @Override
    public void log(UUID customerId, String methodName, AuditingStatus status){
        log(customerId, methodName, status, null);
    }

    @Override
    public void log(UUID customerId, String methodName, AuditingStatus status, String reason) {
        if (status == AuditingStatus.ERROR){
            publisher.publishEvent(new AuditingImmediateEvent(customerId, methodName, status, reason));
        } else {
            publisher.publishEvent(new AuditingMethodEvent(customerId, methodName, status));
        }
    }
}
