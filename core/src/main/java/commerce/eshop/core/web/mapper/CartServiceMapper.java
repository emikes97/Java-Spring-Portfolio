package commerce.eshop.core.web.mapper;

import commerce.eshop.core.model.entity.CartItem;
import commerce.eshop.core.web.dto.response.Cart.DTOCartItemResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CartServiceMapper {

    public DTOCartItemResponse toDto(CartItem c){
        BigDecimal totalPrice = c.getPriceAt().multiply(BigDecimal.valueOf(c.getQuantity()));
        return new DTOCartItemResponse(
                c.getCartItemId(),
                c.getProduct().getProductId(),
                c.getProductName(),
                c.getQuantity(),
                c.getPriceAt(),
                totalPrice,
                c.getAddedAt()
        );
    }
}
