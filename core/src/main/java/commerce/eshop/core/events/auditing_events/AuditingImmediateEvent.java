package commerce.eshop.core.events.auditing_events;

import commerce.eshop.core.util.enums.AuditingStatus;

import java.util.UUID;

public record AuditingImmediateEvent(UUID customerId,
                                     String methodName,
                                     AuditingStatus status,
                                     String reason) {
}
