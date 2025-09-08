package commerce.eshop.core.web.dto.requests.Order;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public record DTOOrderPaymentMethod(@NotBlank @Size(max=60) String name,
                                    @NotBlank @Size(max=60) String surname,
                                    @NotBlank @Size(max=50) String provider,
                                    @NotBlank @Size(max=25) String brand,
                                    @NotBlank @Size(min =13, max=19) String cardNumber,
                                    @NotNull @Min(2025) @Max(2045) Short yearExp,
                                    @NotNull @Min(1) @Max(12) Short monthExp,
                                    @NotBlank @Size(min = 3, max=4) @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String cvv)
{}
