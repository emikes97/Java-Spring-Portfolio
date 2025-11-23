package commerse.eshop.core.application.cartTest.factory;

import commerce.eshop.core.application.cart.factory.CartItemFactory;
import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.CartItem;
import commerce.eshop.core.model.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CartItemFactoryTest {

    private CartItemFactory cartItemFactory;

    @BeforeEach
    void setUp() {
        cartItemFactory = new CartItemFactory();
    }

    @Test
    void newCartItem_success() {
        Cart cart = mock(Cart.class);
        Product product = mock(Product.class);
        String productName = "Laptop";
        int quantity = 2;
        BigDecimal price = new BigDecimal("999.99");

        CartItem item = cartItemFactory.newCartItem(cart, product, productName, quantity, price);

        assertNotNull(item);
        assertEquals(cart, item.getCart());
        assertEquals(product, item.getProduct());
        assertEquals(productName, item.getProductName());
        assertEquals(quantity, item.getQuantity());
        assertEquals(price, item.getPriceAt());
    }
}