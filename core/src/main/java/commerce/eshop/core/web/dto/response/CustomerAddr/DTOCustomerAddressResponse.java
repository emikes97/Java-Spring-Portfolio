package commerce.eshop.core.web.dto.response.CustomerAddr;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record DTOCustomerAddressResponse(@NotNull long id,
                                         @NotBlank String country,
                                         @NotBlank String street,
                                         @NotBlank String city,
                                         @NotBlank String postalCode,
                                         @NotNull boolean isDefault)
                                            {}
