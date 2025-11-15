package commerce.eshop.core.application.wishlist.factory;

import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.model.entity.Wishlist;
import commerce.eshop.core.model.entity.WishlistItem;
import org.springframework.stereotype.Component;

@Component
public class WishItemFactory {

    public WishlistItem create(Wishlist wishlist, Product product, String productName){
        return  new WishlistItem(wishlist, product, productName);
    }
}
