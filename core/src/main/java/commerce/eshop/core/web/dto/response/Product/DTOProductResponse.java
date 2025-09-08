package commerce.eshop.core.web.dto.response.Product;


import java.math.BigDecimal;
import java.util.Map;

public record DTOProductResponse(Long productId,
                                 String productName,
                                 String productDescription,
                                 Map<String, Object> productDetails,
                                 int availableStock,
                                 BigDecimal productPrice
                                 ) {}


