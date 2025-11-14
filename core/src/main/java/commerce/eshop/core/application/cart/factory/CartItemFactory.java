package commerce.eshop.core.application.cart.factory;

import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.CartItem;
import commerce.eshop.core.model.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CartItemFactory {

    public CartItem newCartItem(Cart cart, Product product, String productName, int quantity, BigDecimal productPrice){
        return new CartItem(cart, product, productName, quantity, productPrice);
    }
}
