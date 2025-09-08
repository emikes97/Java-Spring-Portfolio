package commerce.eshop.core.web.dto.requests.Transactions.PaymentVariants;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UseSavedMethod(@NotNull UUID customerPaymentMethodId) implements PaymentInstruction {
}
