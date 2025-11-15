package commerce.eshop.core.application.events.order;

import java.util.UUID;

public record CancelledOrderEvent(UUID customerId, UUID orderId) {
}
