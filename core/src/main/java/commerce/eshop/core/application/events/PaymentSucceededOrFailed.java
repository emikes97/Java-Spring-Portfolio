package commerce.eshop.core.application.events;

import commerce.eshop.core.util.enums.OrderStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/// Event to validate the successfulness or failure of a transaction to update Order.

public record PaymentSucceededOrFailed(OrderStatus status, OffsetDateTime time, UUID orderId) {}
