package commerce.eshop.core.web.dto.response.Cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DTOCartItemResponse(Long cartItemId,
                                  Long productId,
                                  @Size(max = 200) String productName,
                                  @Min(1) int quantity,
                                  BigDecimal unitPrice,  // Price per unit
                                  BigDecimal totalPrice, // Price * quantity
                                  OffsetDateTime addedAt) {}
