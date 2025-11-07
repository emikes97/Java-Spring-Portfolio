package commerce.eshop.core.events.customer;

import java.util.UUID;

public record CustomerSuccessfulUpdatePasswordEvent(UUID customerId, Boolean successfulOrNot) {
}
