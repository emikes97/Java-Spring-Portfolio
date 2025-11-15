package commerce.eshop.core.web.dto.response.Transactions;

import commerce.eshop.core.application.util.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record DTOTransactionResponse(
        UUID transactionId,
        UUID orderId,
        String customerId,
        Map<String, Object> paymentMethod,
        BigDecimal totalOutstanding,
        TransactionStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {}
