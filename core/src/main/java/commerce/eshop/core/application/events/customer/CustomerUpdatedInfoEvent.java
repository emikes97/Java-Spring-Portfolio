package commerce.eshop.core.application.events.customer;

import java.util.UUID;

public record CustomerUpdatedInfoEvent(UUID customerId, String changed) {
}
