package commerce.eshop.core.application.cart.commands;

import commerce.eshop.core.application.cart.validation.AuditedCartValidation;
import commerce.eshop.core.application.cart.writer.CartWriter;
import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.CartItem;
import commerce.eshop.core.service.DomainLookupService;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class RemoveCartItem {

    // == Fields ==
    private final CartWriter cartWriter;
    private final DomainLookupService domainLookupService;
    private final AuditedCartValidation auditedCartValidation;

    // == Constructors ==
    @Autowired
    public RemoveCartItem(CartWriter cartWriter, DomainLookupService domainLookupService, AuditedCartValidation auditedCartValidation) {
        this.cartWriter = cartWriter;
        this.domainLookupService = domainLookupService;
        this.auditedCartValidation = auditedCartValidation;
    }

    // == Public Method ==
    @Transactional
    public void handle(UUID customerId, long productId, Integer quantity){

        final Cart cart = domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.CART_REMOVE);
        CartItem cartItem = domainLookupService.getCartItemOrThrow(cart.getCartId(), productId, customerId, EndpointsNameMethods.CART_REMOVE);

        if (quantity == null || cartItem.getQuantity() <= quantity) {
            cartWriter.delete(cart, productId, EndpointsNameMethods.CART_REMOVE); // -> If quantity null, remove item and leave early
            return;
        }

        auditedCartValidation.checkValidQuantity(quantity, customerId); // -> Check that quantity value is valid

        cartItem.setQuantity(cartItem.getQuantity() - quantity);
        cartWriter.save(cartItem, customerId, EndpointsNameMethods.CART_REMOVE);
    }

    @Transactional
    public void handle(UUID customerId){
        final Cart cart = domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.CART_CLEAR);
        cartWriter.clear(cart, EndpointsNameMethods.CART_CLEAR);
    }
}
