package commerce.eshop.core.application.cart.commands;

import commerce.eshop.core.application.cart.factory.CartItemFactory;
import commerce.eshop.core.application.cart.validation.AuditedCartValidation;
import commerce.eshop.core.application.cart.writer.CartWriter;
import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.CartItem;
import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class AddCartItem {

    // == Constants ==
    private static final int MAX_QTY = 99;

    // == Fields ==
    private final AuditedCartValidation validation;
    private final DomainLookupService domainLookupService;
    private final CartWriter cartWriter;
    private final CartItemFactory factory;

    // == Constructors ==
    @Autowired
    public AddCartItem(AuditedCartValidation validation, DomainLookupService domainLookupService, CartWriter cartWriter, CartItemFactory factory) {
        this.validation = validation;
        this.domainLookupService = domainLookupService;
        this.cartWriter = cartWriter;
        this.factory = factory;
    }

    // == Public Methods ==
    @Transactional
    public CartItem handle(UUID customerId, long productId, int quantity){
        validation.checkValidQuantity(quantity, customerId, MAX_QTY);

        final Product product = domainLookupService.getProductOrThrow(customerId, productId, EndpointsNameMethods.CART_ADD_ITEM);
        final Cart cart = domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.CART_ADD_ITEM);

        int updated = cartWriter.bumpQuantity(cart.getCartId(), productId, quantity, MAX_QTY, customerId);

        CartItem cartItem;

        if(updated == 0){
            // no row existed → insert
            cartItem = factory.newCartItem(cart, product, product.getProductName(), quantity, product.getPrice());
            cartItem = cartWriter.saveItem(cartItem, MAX_QTY);
        } else {
            cartItem = domainLookupService.getCartItemOrThrow(cart.getCartId(), productId, customerId, EndpointsNameMethods.CART_ADD_ITEM);
        }

        return cartItem;
    }
}
