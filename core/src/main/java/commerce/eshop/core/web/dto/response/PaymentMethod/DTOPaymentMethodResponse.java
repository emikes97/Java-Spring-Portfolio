package commerce.eshop.core.web.dto.response.PaymentMethod;

import commerce.eshop.core.util.enums.TokenStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record DTOPaymentMethodResponse(@NotBlank String provider,
                                       @NotBlank String brand,
                                       @NotBlank String last4,
                                       @NotNull short yearExp,
                                       @NotNull short monthExp,
                                       @NotNull TokenStatus status,
                                       boolean isDefault,
                                       @NotNull OffsetDateTime createdAt) {}
