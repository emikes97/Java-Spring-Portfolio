package commerse.eshop.core.service;

import commerse.eshop.core.model.entity.enums.AuditingStatus;
import jakarta.annotation.Nullable;

import java.util.UUID;

public interface AuditingService {
    void log(@Nullable UUID customerId, String methodName, AuditingStatus status, String reason);
}
