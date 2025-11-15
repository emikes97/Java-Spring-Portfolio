package commerce.eshop.core.application.wishlist.writer;

import commerce.eshop.core.model.entity.Wishlist;
import commerce.eshop.core.model.entity.WishlistItem;
import commerce.eshop.core.repository.WishlistItemRepo;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.enums.AuditingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WishlistWriter {

    // == Fields ==
    private final WishlistItemRepo wishItemRepo;
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public WishlistWriter(WishlistItemRepo wishItemRepo, CentralAudit centralAudit) {
        this.wishItemRepo = wishItemRepo;
        this.centralAudit = centralAudit;
    }

    // == Public Methods ==

    public WishlistItem save(WishlistItem item, UUID customerId, String endpoint){
        try {
            item = wishItemRepo.saveAndFlush(item);
            return item;
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, customerId, endpoint, AuditingStatus.ERROR, dup.toString());
        }
    }

    public void delete(WishlistItem item, UUID customerId, String endpoint){
        try {
            wishItemRepo.delete(item);
            wishItemRepo.flush();
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, customerId, endpoint, AuditingStatus.ERROR, dup.toString());
        }
    }

    public void clear(Wishlist wish, UUID customerId, String endpoint){
        try {
            int expected = wishItemRepo.countWishlistItems(wish.getWishlistId());
            int updated = wishItemRepo.clearWishlist(wish.getWishlistId());

            if (updated != expected) {
                throw centralAudit.audit(new IllegalStateException("Data mismatch"), customerId, endpoint,
                        AuditingStatus.ERROR, "DATA_MISMATCH");
            }

            wishItemRepo.flush();
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, customerId, endpoint, AuditingStatus.ERROR, dup.toString());
        }
    }
}
