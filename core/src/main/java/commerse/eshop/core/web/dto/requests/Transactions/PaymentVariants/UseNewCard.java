package commerse.eshop.core.web.dto.requests.Transactions.PaymentVariants;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UseNewCard(
        @NotBlank String panMasked,
        @NotBlank String brand,
        @Min(1) @Max(12) int expMonth,
        @Min(2025) int expYear,
        @NotBlank String holderName,
        String cvc
) implements PaymentInstruction {
}
