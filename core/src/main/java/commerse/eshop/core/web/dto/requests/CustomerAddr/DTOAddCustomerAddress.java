package commerse.eshop.core.web.dto.requests.CustomerAddr;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DTOAddCustomerAddress(@NotBlank @Size(max=150) String country,
                                    @NotBlank @Size(max=100) String street,
                                    @NotBlank @Size(max=75) String city,
                                    @NotBlank @Size(max=50) String postalCode,
                                    @NotNull boolean isDefault) {}