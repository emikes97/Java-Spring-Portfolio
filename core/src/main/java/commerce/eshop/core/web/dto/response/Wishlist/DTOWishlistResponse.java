package commerce.eshop.core.web.dto.response.Wishlist;

import java.time.OffsetDateTime;

public record DTOWishlistResponse(
        long wishId,
        long productId,
        String productName,
        OffsetDateTime addedAt
) {}
