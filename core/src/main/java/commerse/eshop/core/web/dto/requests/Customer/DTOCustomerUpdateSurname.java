package commerse.eshop.core.web.dto.requests.Customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DTOCustomerUpdateSurname(@NotBlank @Size(min=2, max=100) String lastName,
                                       @NotBlank String password) {
}
