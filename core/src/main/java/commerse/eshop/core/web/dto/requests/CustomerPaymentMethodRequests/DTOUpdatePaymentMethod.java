package commerse.eshop.core.web.dto.requests.CustomerPaymentMethodRequests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record DTOUpdatePaymentMethod(@Size(max=50) String provider,
                                     @Size(max=25) String brand,
                                     @Size(min =4, max=4) String last4,
                                     Short yearExp,
                                     @Min(1)@Max(12)Short monthExp,
                                     Boolean isDefault) {
}
