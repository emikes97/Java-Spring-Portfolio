package commerce.eshop.core.tests.application.wishlistTest.commands;

import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.wishlist.commands.RemoveWish;
import commerce.eshop.core.application.wishlist.writer.WishlistWriter;
import commerce.eshop.core.model.entity.Wishlist;
import commerce.eshop.core.model.entity.WishlistItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RemoveWishTest {

    // == Fields ==
    private WishlistWriter wishlistWriter;
    private DomainLookupService domainLookupService;
    private RemoveWish removeWish;

    @BeforeEach
    void setUp() {
        wishlistWriter = mock(WishlistWriter.class);
        domainLookupService = mock(DomainLookupService.class);

        removeWish = new RemoveWish(wishlistWriter, domainLookupService);
    }

    @Test
    void handle_removeSingleWish_success() {
        UUID customerId = UUID.randomUUID();
        long wishId = 14L;
        Wishlist wishlist = mock(Wishlist.class);
        WishlistItem wishlistItem = mock(WishlistItem.class);

        when(domainLookupService.getWishlistOrThrow(customerId, EndpointsNameMethods.REMOVE_WISH))
                .thenReturn(wishlist);
        when(domainLookupService.getWishOrThrow(
                customerId,
                wishlist,
                wishId,
                EndpointsNameMethods.REMOVE_WISH
        )).thenReturn(wishlistItem);

        removeWish.handle(customerId, wishId);

        verify(domainLookupService, times(1))
                .getWishlistOrThrow(customerId, EndpointsNameMethods.REMOVE_WISH);
        verify(domainLookupService, times(1))
                .getWishOrThrow(customerId, wishlist, wishId, EndpointsNameMethods.REMOVE_WISH);
        verify(wishlistWriter, times(1))
                .delete(wishlistItem, customerId, EndpointsNameMethods.REMOVE_WISH);
    }

    @Test
    void handle_clearWishlist_success() {
        UUID customerId = UUID.randomUUID();
        Wishlist wishlist = mock(Wishlist.class);

        when(domainLookupService.getWishlistOrThrow(customerId, EndpointsNameMethods.CLEAR_WISHLIST))
                .thenReturn(wishlist);

        removeWish.handle(customerId);

        verify(domainLookupService, times(1))
                .getWishlistOrThrow(customerId, EndpointsNameMethods.CLEAR_WISHLIST);
        verify(wishlistWriter, times(1))
                .clear(wishlist, customerId, EndpointsNameMethods.CLEAR_WISHLIST);
    }
}