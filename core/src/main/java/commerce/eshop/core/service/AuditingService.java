package commerce.eshop.core.service;

import commerce.eshop.core.util.enums.AuditingStatus;
import jakarta.annotation.Nullable;

import java.util.UUID;

public interface AuditingService {
    void log(@Nullable UUID customerId, String methodName, AuditingStatus status, String reason);
}
