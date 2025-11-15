package commerce.eshop.core.application.wishlist.commands;

import commerce.eshop.core.application.wishlist.writer.WishlistWriter;
import commerce.eshop.core.model.entity.Wishlist;
import commerce.eshop.core.model.entity.WishlistItem;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class RemoveWish {

    // == Fields ==
    private final WishlistWriter writer;
    private final DomainLookupService domainLookupService;

    // == Constructors ==
    @Autowired
    public RemoveWish(WishlistWriter writer, DomainLookupService domainLookupService) {
        this.writer = writer;
        this.domainLookupService = domainLookupService;
    }

    // == Public methods ==
    @Transactional
    public void handle(UUID customerId, long wishId){
        final Wishlist wishlist = domainLookupService.getWishlistOrThrow(customerId, EndpointsNameMethods.REMOVE_WISH);
        final WishlistItem wishlistItem = domainLookupService.getWishOrThrow(customerId, wishlist, wishId, EndpointsNameMethods.REMOVE_WISH);
        writer.delete(wishlistItem, customerId, EndpointsNameMethods.REMOVE_WISH);
    }

    @Transactional
    public void handle(UUID customerId){
        final Wishlist wishlist = domainLookupService.getWishlistOrThrow(customerId, EndpointsNameMethods.CLEAR_WISHLIST);
        writer.clear(wishlist, customerId, EndpointsNameMethods.CLEAR_WISHLIST);
    }
}
