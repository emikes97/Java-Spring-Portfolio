package commerse.eshop.core.events;

import java.util.UUID;

public record PaymentMethodCreatedEvent(
        UUID customerId,
        UUID paymentId,
        String provider) {}
