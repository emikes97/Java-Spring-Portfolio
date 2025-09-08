package commerce.eshop.core.web.dto.requests.Products;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Map;

public record DTOAddProduct(@NotBlank @Size(max = 200) String productName,
                            @NotBlank @Size(max = 255) String productDescription,
                            @NotNull Map<String, Object> productDetails,
                            @NotNull @Min(0) int productAvailableStock,
                            @NotNull @DecimalMin("0.001") BigDecimal productPrice,
                            boolean isActive) {
}
