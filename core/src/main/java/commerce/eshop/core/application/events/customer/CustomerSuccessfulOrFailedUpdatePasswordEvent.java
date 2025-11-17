package commerce.eshop.core.application.events.customer;

import java.util.UUID;

public record CustomerSuccessfulOrFailedUpdatePasswordEvent(UUID customerId, Boolean successfulOrNot) {
}
