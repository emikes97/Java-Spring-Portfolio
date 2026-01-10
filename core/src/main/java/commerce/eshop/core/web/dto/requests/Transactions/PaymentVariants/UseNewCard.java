package commerce.eshop.core.web.dto.requests.Transactions.PaymentVariants;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

///  === Tokenization should be handled by frontend, due to async design for backend ===
public record UseNewCard(
        @NotBlank String brand,
        @NotBlank String tokenRef,
        @Min(1) @Max(12) int expMonth,
        @Min(2025) int expYear,
        @NotBlank String holderName
) implements PaymentInstruction {
}
