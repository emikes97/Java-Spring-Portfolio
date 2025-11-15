package commerce.eshop.core.application.events.email;

import commerce.eshop.core.application.email.enums.EmailKind;
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

