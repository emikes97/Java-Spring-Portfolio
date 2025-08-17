package commerse.eshop.core.web.dto.requests.CustomerPaymentMethodRequests;

import jakarta.validation.constraints.*;

public record DTOAddPaymentMethod(@NotBlank @Size(max=50) String provider,
                                  @NotBlank @Size(max=25) String brand,
                                  @NotBlank @Size(min =4, max=4) String last4,
                                  @NotNull Short yearExp,
                                  @NotNull @Min(1) @Max(12) Short monthExp,
                                  Boolean isDefault) {}