package commerce.eshop.core.application.cartTest.queries;

import commerce.eshop.core.application.cart.queries.CartQueries;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.util.SortSanitizer;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.sort.CartSort;
import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.CartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartQueriesTest {

    private DomainLookupService domainLookupService;
    private SortSanitizer sortSanitizer;
    private CartQueries cartQueries;

    @BeforeEach
    void setUp() {
        domainLookupService = mock(DomainLookupService.class);
        sortSanitizer = mock(SortSanitizer.class);

        cartQueries = new CartQueries(domainLookupService, sortSanitizer);
    }

    @Test
    void getPagedCartItems() {
        UUID customerId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        Pageable pageable = mock(Pageable.class);
        Pageable sanitized = mock(Pageable.class);
        Cart cart = mock(Cart.class);
        Page<CartItem> item = mock(Page.class);

        when(sortSanitizer.sanitize(
                eq(pageable),
                eq(CartSort.CART_ITEMS_SORT_WHITELIST),
                eq(CartSort.MAX_PAGE_SIZE)
        )).thenReturn(sanitized);
        when(domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.CART_VIEW_ALL))
                .thenReturn(cart);
        when(cart.getCartId()).thenReturn(cartId);
        when(domainLookupService.getPagedCartItems(cartId, sanitized))
                .thenReturn(item);

        Page<CartItem> result = cartQueries.getPagedCartItems(customerId, pageable);

        assertSame(item, result);

        verify(sortSanitizer, times(1)).sanitize(
                eq(pageable),
                eq(CartSort.CART_ITEMS_SORT_WHITELIST),
                eq(CartSort.MAX_PAGE_SIZE)
        );
        verify(domainLookupService, times(1))
                .getCartOrThrow(customerId, EndpointsNameMethods.CART_VIEW_ALL);
        verify(domainLookupService, times(1))
                .getPagedCartItems(cartId, sanitized);
    }

    @Test
    void getCartItem() {
        UUID customerId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        long productId = 255L;
        Cart cart = mock(Cart.class);
        CartItem item = mock(CartItem.class);

        when(domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.CART_FIND_ITEM))
                .thenReturn(cart);
        when(cart.getCartId()).thenReturn(cartId);
        when(domainLookupService.getCartItemOrThrow(
                cartId,
                productId,
                customerId,
                EndpointsNameMethods.CART_FIND_ITEM
        )).thenReturn(item);

        CartItem result = cartQueries.getCartItem(customerId, productId);

        assertSame(item, result);

        verify(domainLookupService, times(1))
                .getCartOrThrow(customerId, EndpointsNameMethods.CART_FIND_ITEM);

        verify(domainLookupService, times(1))
                .getCartItemOrThrow(cartId, productId, customerId, EndpointsNameMethods.CART_FIND_ITEM);
    }
}