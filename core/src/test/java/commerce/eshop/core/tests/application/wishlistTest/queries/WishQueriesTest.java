package commerce.eshop.core.tests.application.wishlistTest.queries;

import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.util.SortSanitizer;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.sort.WishlistSort;
import commerce.eshop.core.application.wishlist.queries.WishQueries;
import commerce.eshop.core.model.entity.Wishlist;
import commerce.eshop.core.model.entity.WishlistItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class WishQueriesTest {

    // == Fields ==
    private SortSanitizer sortSanitizer;
    private DomainLookupService domainLookupService;
    private WishQueries wishQueries;

    // == Tests ==

    @BeforeEach
    void setUp() {
        sortSanitizer = mock(SortSanitizer.class);
        domainLookupService = mock(DomainLookupService.class);

        wishQueries = new WishQueries(sortSanitizer, domainLookupService);
    }

    @Test
    void getAllPagedWishlistItems_success() {
        UUID customerId = UUID.randomUUID();
        Pageable originalPage = mock(Pageable.class);
        Pageable sanitizedPage = mock(Pageable.class);
        Wishlist wishlist = mock(Wishlist.class);
        UUID wishlistId = UUID.randomUUID();
        when(wishlist.getWishlistId()).thenReturn(wishlistId);
        Page<WishlistItem> items = mock(Page.class);

        when(sortSanitizer.sanitize(
                eq(originalPage),
                eq(WishlistSort.WISHLIST_SORT_WHITELIST),
                eq(WishlistSort.MAX_PAGE_SIZE)
        )).thenReturn(sanitizedPage);
        when(domainLookupService.getWishlistOrThrow(customerId, EndpointsNameMethods.FIND_ALL_WISHES))
                .thenReturn(wishlist);
        when(domainLookupService.getPagedWishItems(wishlistId, sanitizedPage))
                .thenReturn(items);

        Page<WishlistItem> result = wishQueries.getAllPagedWishlistItems(customerId, originalPage);

        assertSame(items, result);

        verify(sortSanitizer, times(1)).sanitize(
                eq(originalPage),
                eq(WishlistSort.WISHLIST_SORT_WHITELIST),
                eq(WishlistSort.MAX_PAGE_SIZE)
        );
        verify(domainLookupService, times(1))
                .getWishlistOrThrow(customerId, EndpointsNameMethods.FIND_ALL_WISHES);
        verify(domainLookupService, times(1))
                .getPagedWishItems(wishlistId, sanitizedPage);
    }

    @Test
    void getWishlistItem_success() {
        UUID customerId = UUID.randomUUID();
        long wishId = 42L;
        Wishlist wishlist = mock(Wishlist.class);
        WishlistItem expectedItem = mock(WishlistItem.class);

        when(domainLookupService.getWishlistOrThrow(customerId, EndpointsNameMethods.FIND_WISH))
                .thenReturn(wishlist);
        when(domainLookupService.getWishOrThrow(
                customerId,
                wishlist,
                wishId,
                EndpointsNameMethods.FIND_WISH
        )).thenReturn(expectedItem);

        WishlistItem result = wishQueries.getWishlistItem(customerId, wishId);

        assertSame(expectedItem, result);

        verify(domainLookupService, times(1))
                .getWishlistOrThrow(customerId, EndpointsNameMethods.FIND_WISH);
        verify(domainLookupService, times(1))
                .getWishOrThrow(customerId, wishlist, wishId, EndpointsNameMethods.FIND_WISH);
    }
}