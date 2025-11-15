package commerce.eshop.core.application.infrastructure.domain;

import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.CartItem;
import commerce.eshop.core.repository.CartItemRepo;
import commerce.eshop.core.repository.CartRepo;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.UUID;

@Component
public class CartDomain {

    // == Fields ==
    private final CartItemRepo ciRepo;
    private final CartRepo cRepo;
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public CartDomain(CartItemRepo ciRepo, CartRepo cRepo, CentralAudit centralAudit) {
        this.ciRepo = ciRepo;
        this.cRepo = cRepo;
        this.centralAudit = centralAudit;
    }

    // == Public Methods ==

    public Cart retrieveCart(UUID customerId, String method){
        try {
            return cRepo.findCartByCustomerId(customerId).orElseThrow(() -> new NoSuchElementException("Cart doesn't exist"));
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e,customerId, method, AuditingStatus.ERROR, e.toString());
        }
    }

    public CartItem retrieveCartItem(UUID cartId, long productId, UUID customerId, String method){
        try{
            return ciRepo.getCartItemByCartIdAndProductId(cartId, productId).orElseThrow(
                    () -> new NoSuchElementException("Cart Item doesn't exist"));
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.ERROR, e.toString());
        }
    }

    // == Paged Queries ==
    public Page<CartItem> retrievedPagedCartItems(UUID cartId, Pageable page){
        return ciRepo.findByCart_CartId(cartId, page);
    }
}
