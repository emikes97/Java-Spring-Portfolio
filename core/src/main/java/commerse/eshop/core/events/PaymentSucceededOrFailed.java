package commerse.eshop.core.events;

import commerse.eshop.core.model.entity.enums.OrderStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentSucceededOrFailed(OrderStatus status, OffsetDateTime time, UUID orderId) {}
