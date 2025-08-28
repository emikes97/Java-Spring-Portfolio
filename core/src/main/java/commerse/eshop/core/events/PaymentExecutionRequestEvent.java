package commerse.eshop.core.events;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record PaymentExecutionRequestEvent(UUID transactionId,
                                           UUID orderId,
                                           UUID customerId,
                                           String methodType,
                                           Map<String, Object> snapshot,
                                           BigDecimal amount,
                                           String idemKey) {}
