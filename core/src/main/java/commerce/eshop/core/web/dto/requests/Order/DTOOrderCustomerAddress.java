package commerce.eshop.core.web.dto.requests.Order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DTOOrderCustomerAddress(@NotBlank @Size(max=150) String country,
                                      @NotBlank @Size(max=100) String street,
                                      @NotBlank @Size(max=75) String city,
                                      @NotBlank @Size(max=50) String postalCode) {
}
