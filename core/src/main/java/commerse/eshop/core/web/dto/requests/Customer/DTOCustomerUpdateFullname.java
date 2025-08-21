package commerse.eshop.core.web.dto.requests.Customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DTOCustomerUpdateFullname(
        @NotBlank @Size(min=2, max=100) String name,
        @NotBlank @Size(min=2, max=100) String surname,
        @NotBlank String password) {}
