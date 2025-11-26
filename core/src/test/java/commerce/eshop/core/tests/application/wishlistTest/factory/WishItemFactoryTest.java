package commerce.eshop.core.tests.application.wishlistTest.factory;

import commerce.eshop.core.application.wishlist.factory.WishItemFactory;
import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.model.entity.Wishlist;
import commerce.eshop.core.model.entity.WishlistItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class WishItemFactoryTest {

    // == Fields ==
    private WishItemFactory wishItemFactory;

    @BeforeEach
    void setUp() {
        wishItemFactory = new WishItemFactory();
    }

    @Test
    void create_success() {
        Wishlist wishlist = mock(Wishlist.class);
        Product product = mock(Product.class);

        String productName = "Test Product";

        WishlistItem result = wishItemFactory.create(wishlist, product, productName);

        assertNotNull(result);
        assertEquals(wishlist, result.getWishlist());
        assertEquals(product, result.getProduct());
        assertEquals(productName, result.getProductName());
    }
}