package commerce.eshop.core.application.events.order;

import java.math.BigDecimal;
import java.util.UUID;

public record PlacedOrderEvent(UUID customerId, UUID orderId, BigDecimal total_outstanding, String currency) {}
