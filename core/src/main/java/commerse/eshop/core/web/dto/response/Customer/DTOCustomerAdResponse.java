package commerse.eshop.core.web.dto.response.Customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DTOCustomerAdResponse(String country,
                                    String street,
                                    String city,
                                    String postalCode) {
}
