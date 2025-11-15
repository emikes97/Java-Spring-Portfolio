package commerce.eshop.core.application.wishlist.commands;

import commerce.eshop.core.application.wishlist.factory.WishItemFactory;
import commerce.eshop.core.application.wishlist.writer.WishlistWriter;
import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.model.entity.Wishlist;
import commerce.eshop.core.model.entity.WishlistItem;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class AddWish {

    // == Fields ==
    private final DomainLookupService domainLookupService;
    private final WishlistWriter writer;
    private final WishItemFactory factory;

    // == Constructors ==
    @Autowired
    public AddWish(DomainLookupService domainLookupService, WishlistWriter writer, WishItemFactory factory) {
        this.domainLookupService = domainLookupService;
        this.writer = writer;
        this.factory = factory;
    }

    // == Public Methods ==
    @Transactional
    public WishlistItem handle(UUID customerId, long productId){
        final Product product = domainLookupService.getProductOrThrow(customerId, productId, EndpointsNameMethods.ADD_NEW_WISH);
        final Wishlist wishlist = domainLookupService.getWishlistOrThrow(customerId, EndpointsNameMethods.ADD_NEW_WISH);
        WishlistItem item = factory.create(wishlist, product, product.getProductName());
        item = writer.save(item, customerId, EndpointsNameMethods.ADD_NEW_WISH);
        return item;
    }
}
