package commerce.eshop.core.application.cart.writer;

import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.CartItem;
import commerce.eshop.core.repository.CartItemRepo;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.UUID;

@Component
public class CartWriter {

    // == Fields ==
    private final CartItemRepo cartItemRepo;
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public CartWriter(CartItemRepo cartItemRepo, CentralAudit centralAudit) {
        this.cartItemRepo = cartItemRepo;
        this.centralAudit = centralAudit;
    }

    // == Public Methods ==

    public int bumpQuantity(UUID cartId, long productId, int inc, int MAX_QUANTITY, UUID customerId){
        try {
            return cartItemRepo.bumpQuantity(cartId, productId, inc, MAX_QUANTITY);
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, customerId, EndpointsNameMethods.CART_ADD_ITEM, AuditingStatus.ERROR, dup.toString());
        }
    }

    public CartItem saveItem(CartItem item, int MAX_QTY){
        try {
            item = cartItemRepo.saveAndFlush(item);
            return item;
        } catch (DataIntegrityViolationException dup){
            // another thread inserted first → bump again
            cartItemRepo.bumpQuantity(item.getCart().getCartId(), item.getProduct().getProductId(), item.getQuantity(), MAX_QTY);
            item = cartItemRepo.getCartItemByCartIdAndProductId(item.getCart().getCartId(), item.getProduct().getProductId()).orElseThrow();
            return item;
        }
    }

    public CartItem save(CartItem item, UUID customerId, String endpoint){
        try {
            item = cartItemRepo.saveAndFlush(item);
            return item;
        }catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, customerId, endpoint, AuditingStatus.ERROR);
        }
    }

    public void clear(Cart cart, String endpoint){
        try {
            cartItemRepo.clearCart(cart.getCartId());
        } catch (NoSuchElementException ex){
            throw centralAudit.audit(ex, cart.getCustomer().getCustomerId(), endpoint, AuditingStatus.WARNING, ex.toString());
        }
    }

    public void delete(Cart cart, long productId, String endpoint){
        try {
            cartItemRepo.deleteItemByCartIdAndProductId(cart.getCartId(), productId);
        } catch (NoSuchElementException ex){
            throw centralAudit.audit(ex, cart.getCustomer().getCustomerId(), endpoint, AuditingStatus.WARNING, ex.toString());
        }
    }
}
