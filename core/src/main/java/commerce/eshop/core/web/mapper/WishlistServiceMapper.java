package commerce.eshop.core.web.mapper;

import commerce.eshop.core.model.entity.WishlistItem;
import commerce.eshop.core.web.dto.response.Wishlist.DTOWishlistResponse;
import org.springframework.stereotype.Component;

@Component
public class WishlistServiceMapper {

    public DTOWishlistResponse toDto(WishlistItem wi) {
        return new DTOWishlistResponse(
                wi.getWishId(),
                wi.getProduct().getProductId(),   // id only
                wi.getProductName(),              // snapshot
                wi.getAddedAt()
        );
    }
}
