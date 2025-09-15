package commerce.eshop.core.events;

import commerce.eshop.core.util.enums.EmailKind;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EmailEventRequest(
        @NotNull UUID customerId,
        UUID orderOrPaymentId,
        String customerName,
        @NotBlank @Email String toEmail,
        @NotNull EmailKind type,
        @NotBlank String subject,
        @NotBlank String emailText
        ) {
}

