package commerce.eshop.core.application.cart.queries;

import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.CartItem;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.util.SortSanitizer;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.sort.CartSort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class CartQueries {

    // == Fields ==
    private final DomainLookupService domainLookupService;
    private final SortSanitizer sortSanitizer;

    // == Constructors ==
    @Autowired
    public CartQueries(DomainLookupService domainLookupService, SortSanitizer sortSanitizer) {
        this.domainLookupService = domainLookupService;
        this.sortSanitizer = sortSanitizer;
    }

    // == Public Methods ==
    @Transactional(readOnly = true)
    public Page<CartItem> getPagedCartItems(UUID customerId, Pageable pageable){
        Pageable p = sortSanitizer.sanitize(pageable, CartSort.CART_ITEMS_SORT_WHITELIST, CartSort.MAX_PAGE_SIZE);
        final Cart cart = domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.CART_VIEW_ALL);
        Page<CartItem> items = domainLookupService.getPagedCartItems(cart.getCartId(), p);
        return items;
    }

    @Transactional(readOnly = true)
    public CartItem getCartItem(UUID customerId, long productId){
        final Cart cart = domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.CART_FIND_ITEM);
        final CartItem cartItem = domainLookupService.getCartItemOrThrow(cart.getCartId(), productId, customerId, EndpointsNameMethods.CART_FIND_ITEM);
        return cartItem;
    }
}
