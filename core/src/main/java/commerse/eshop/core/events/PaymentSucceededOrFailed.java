package commerse.eshop.core.events;

import commerse.eshop.core.model.entity.enums.OrderStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/// Event to validate the successfulness or failure of a transaction to update Order.

public record PaymentSucceededOrFailed(OrderStatus status, OffsetDateTime time, UUID orderId) {}
