package commerce.eshop.core.application.wishlist.queries;

import commerce.eshop.core.model.entity.Wishlist;
import commerce.eshop.core.model.entity.WishlistItem;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.util.SortSanitizer;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.sort.WishlistSort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class WishQueries {

    // == Fields ==
    private final SortSanitizer sortSanitizer;
    private final DomainLookupService domainLookupService;

    // == Constructors ==
    @Autowired
    public WishQueries(SortSanitizer sortSanitizer, DomainLookupService domainLookupService) {
        this.sortSanitizer = sortSanitizer;
        this.domainLookupService = domainLookupService;
    }

    // == Public Methods ==
    @Transactional(readOnly = true)
    public Page<WishlistItem> getAllPagedWishlistItems(UUID customerId, Pageable page){
        Pageable p = sortSanitizer.sanitize(page, WishlistSort.WISHLIST_SORT_WHITELIST, WishlistSort.MAX_PAGE_SIZE);
        Wishlist wishlist = domainLookupService.getWishlistOrThrow(customerId, EndpointsNameMethods.FIND_ALL_WISHES);
        return domainLookupService.getPagedWishItems(wishlist.getWishlistId(), p);
    }

    @Transactional(readOnly = true)
    public WishlistItem getWishlistItem(UUID customerId, long wishId){
        final Wishlist wishlist = domainLookupService.getWishlistOrThrow(customerId, EndpointsNameMethods.FIND_WISH);
        return domainLookupService.getWishOrThrow(customerId, wishlist, wishId, EndpointsNameMethods.FIND_WISH);
    }
}
