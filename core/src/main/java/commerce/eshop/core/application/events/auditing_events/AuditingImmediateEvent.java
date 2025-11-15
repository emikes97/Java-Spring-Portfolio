package commerce.eshop.core.application.events.auditing_events;

import commerce.eshop.core.application.util.enums.AuditingStatus;

import java.util.UUID;

public record AuditingImmediateEvent(UUID customerId,
                                     String methodName,
                                     AuditingStatus status,
                                     String reason) {
}
