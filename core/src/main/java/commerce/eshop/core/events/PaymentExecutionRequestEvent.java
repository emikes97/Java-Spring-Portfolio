package commerce.eshop.core.events;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/// Publish event to start the payment execution request.

public record PaymentExecutionRequestEvent(UUID transactionId,
                                           UUID orderId,
                                           UUID customerId,
                                           String methodType,
                                           Map<String, Object> snapshot,
                                           BigDecimal amount,
                                           String idemKey) {}
