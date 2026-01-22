package commerce.eshop.core.application.events.order;

import jakarta.annotation.Nullable;

import java.math.BigDecimal;
import java.util.UUID;

public record PlacedOrderEvent(UUID customerId, UUID orderId, BigDecimal total_outstanding, String currency, @Nullable UUID idemkey, @Nullable Long jobId) {}
