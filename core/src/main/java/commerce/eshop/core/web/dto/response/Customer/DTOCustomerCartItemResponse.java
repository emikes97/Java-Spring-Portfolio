package commerce.eshop.core.web.dto.response.Customer;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DTOCustomerCartItemResponse(long cartItemId,
                                          UUID cartId,
                                          long productId,
                                          String productName,
                                          int quantity,
                                          BigDecimal priceAt,
                                          OffsetDateTime addedAt) {
}