package commerse.eshop.core.web.dto.response.Order;


import java.math.BigDecimal;

public record DTOOrderItemsResponse(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal priceAt) {}