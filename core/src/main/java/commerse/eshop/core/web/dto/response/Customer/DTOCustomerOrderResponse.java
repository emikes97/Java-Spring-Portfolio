package commerse.eshop.core.web.dto.response.Customer;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DTOCustomerOrderResponse(UUID orderId,
                                       UUID customerId,
                                       BigDecimal totalOutstanding,
                                       DTOCustomerAdResponse addressToSend,
                                       OffsetDateTime orderCreatedAt,
                                       OffsetDateTime orderCompletedAt) {
}

