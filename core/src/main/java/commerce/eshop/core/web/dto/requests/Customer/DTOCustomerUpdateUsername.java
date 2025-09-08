package commerce.eshop.core.web.dto.requests.Customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DTOCustomerUpdateUsername(
        @NotBlank @Size(min=3, max=50) String username,
        @NotBlank String password) {}
