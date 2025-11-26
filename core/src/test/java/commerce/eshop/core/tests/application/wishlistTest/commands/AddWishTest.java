package commerce.eshop.core.tests.application.wishlistTest.commands;

import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.wishlist.commands.AddWish;
import commerce.eshop.core.application.wishlist.factory.WishItemFactory;
import commerce.eshop.core.application.wishlist.writer.WishlistWriter;
import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.model.entity.Wishlist;
import commerce.eshop.core.model.entity.WishlistItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddWishTest {

    // == Fields ==
    private DomainLookupService domainLookupService;
    private WishlistWriter wishlistWriter;
    private WishItemFactory wishItemFactory;
    private AddWish addWish;

    @BeforeEach
    void setUp() {
        domainLookupService = mock(DomainLookupService.class);
        wishlistWriter = mock(WishlistWriter.class);
        wishItemFactory = mock(WishItemFactory.class);

        addWish = new AddWish(domainLookupService, wishlistWriter, wishItemFactory);
    }

    @Test
    void handle_success() {
        UUID customerId = UUID.randomUUID();
        long productId = 42L;

        Product product = mock(Product.class);
        Wishlist wishlist = mock(Wishlist.class);
        WishlistItem createdItem = mock(WishlistItem.class);
        WishlistItem savedItem = mock(WishlistItem.class);

        String productName = "Test Product";

        when(domainLookupService.getProductOrThrow(customerId, productId, EndpointsNameMethods.ADD_NEW_WISH))
                .thenReturn(product);
        when(domainLookupService.getWishlistOrThrow(customerId, EndpointsNameMethods.ADD_NEW_WISH))
                .thenReturn(wishlist);
        when(product.getProductName()).thenReturn(productName);
        when(wishItemFactory.create(wishlist, product, productName)).thenReturn(createdItem);
        when(wishlistWriter.save(createdItem, customerId, EndpointsNameMethods.ADD_NEW_WISH)).thenReturn(savedItem);

        WishlistItem result = addWish.handle(customerId, productId);

        assertSame(savedItem, result);

        verify(domainLookupService, times(1))
                .getProductOrThrow(customerId, productId, EndpointsNameMethods.ADD_NEW_WISH);
        verify(domainLookupService, times(1))
                .getWishlistOrThrow(customerId, EndpointsNameMethods.ADD_NEW_WISH);
        verify(wishItemFactory, times(1)).create(wishlist, product, productName);
        verify(wishlistWriter, times(1))
                .save(createdItem, customerId, EndpointsNameMethods.ADD_NEW_WISH);
    }
}