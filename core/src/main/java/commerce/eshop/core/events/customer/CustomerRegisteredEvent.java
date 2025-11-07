package commerce.eshop.core.events.customer;

import java.util.UUID;

public record CustomerRegisteredEvent(UUID customerId) {
}
