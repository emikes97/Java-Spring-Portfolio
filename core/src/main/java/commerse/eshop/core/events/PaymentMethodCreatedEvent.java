package commerse.eshop.core.events;

import java.util.UUID;

/// Event to start the tokenization

public record PaymentMethodCreatedEvent(
        UUID customerId,
        UUID paymentId,
        String provider) {}
