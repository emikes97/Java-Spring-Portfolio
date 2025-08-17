package commerse.eshop.core.web.dto.response.PaymentMethod;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record DTOPaymentMethodResponse(@NotBlank String provider,
                                       @NotBlank String brand,
                                       @NotBlank String last4,
                                       @NotNull short yearExp,
                                       @NotNull short monthExp,
                                       boolean isDefault,
                                       @NotNull OffsetDateTime createdAt) {}
