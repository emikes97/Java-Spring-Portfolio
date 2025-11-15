package commerce.eshop.core.application.infrastructure.domain;

import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.model.entity.Wishlist;
import commerce.eshop.core.model.entity.WishlistItem;
import commerce.eshop.core.repository.WishlistItemRepo;
import commerce.eshop.core.repository.WishlistRepo;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.UUID;

@Component
public class WishlistDomain {

    // == Fields ==
    private final WishlistRepo wRepo;
    private final WishlistItemRepo wiRepo;
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public WishlistDomain(WishlistRepo wRepo, WishlistItemRepo wiRepo, CentralAudit centralAudit) {
        this.wRepo = wRepo;
        this.wiRepo = wiRepo;
        this.centralAudit = centralAudit;
    }

    // == Public methods ==
    public Wishlist retrieveWishlist(UUID customerId, String method){
        try {
            return wRepo.findWishlistByCustomerId(customerId).orElseThrow(
                    () -> new NoSuchElementException("NOT_FOUND_BY_ID")
            );
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.WARNING, e.toString());
        }
    }

    public WishlistItem retrieveWishlistItem(UUID customerId, Wishlist wishlist, long wishId, String method){
        try {
            return wiRepo.findWish(wishlist.getWishlistId(), wishId).orElseThrow(
                    () -> new NoSuchElementException("WISHLIST_ITEM_NOT_FOUND")
            );
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.WARNING, e.toString());
        }
    }

    // == Paged Queries ==
    public Page<WishlistItem> retrievedPagedWishlistItems(UUID wishlist, Pageable page){
        return wiRepo.findByWishlist_WishlistId(wishlist, page);
    }
}
