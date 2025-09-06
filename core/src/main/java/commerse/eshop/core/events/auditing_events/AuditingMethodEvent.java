package commerse.eshop.core.events.auditing_events;

import commerse.eshop.core.model.entity.enums.AuditingStatus;

import java.util.UUID;

public record AuditingMethodEvent(
        UUID customerId,
        String methodName,
        AuditingStatus status,
        String message
) {}
