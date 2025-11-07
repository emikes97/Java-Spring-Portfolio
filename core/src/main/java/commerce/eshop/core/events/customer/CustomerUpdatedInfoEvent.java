package commerce.eshop.core.events.customer;

import java.util.UUID;

public record CustomerUpdatedInfoEvent(UUID customerId, String changed) {
}
