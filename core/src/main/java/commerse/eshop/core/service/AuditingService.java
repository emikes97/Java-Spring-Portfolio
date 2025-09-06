package commerse.eshop.core.service;

import commerse.eshop.core.model.entity.enums.AuditingStatus;

import java.util.UUID;

public interface AuditingService {
    void log(UUID customerId, String methodName, AuditingStatus status);
    void log(UUID customerId, String methodName, AuditingStatus status, String reason);
}
