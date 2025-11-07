package commerce.eshop.core.events.customer;

import java.util.UUID;

public record CustomerFailedUpdatePasswordEvent(UUID customerId, Boolean successfulOrNot) {
}
