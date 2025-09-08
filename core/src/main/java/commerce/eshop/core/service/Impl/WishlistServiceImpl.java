package commerce.eshop.core.service.Impl;

import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.repository.ProductRepo;
import commerce.eshop.core.repository.WishlistItemRepo;
import commerce.eshop.core.repository.WishlistRepo;
import commerce.eshop.core.service.AuditingService;
import commerce.eshop.core.service.WishlistService;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.web.dto.requests.Wishlist.DTOWishlistRequest;
import commerce.eshop.core.web.dto.response.Wishlist.DTOWishlistResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class WishlistServiceImpl implements WishlistService {

    // == Fields ==
    private final CentralAudit centralAudit;
    private final WishlistItemRepo wishlistItemRepo;
    private final WishlistRepo wishlistRepo;
    private final ProductRepo productRepo;

    // == Whitelist & Constraints ==

    // == Constructors ==
    public WishlistServiceImpl(CentralAudit centralAudit, WishlistRepo wishlistRepo, WishlistItemRepo wishlistItemRepo,
                               ProductRepo productRepo){

        this.centralAudit = centralAudit;
        this.wishlistRepo = wishlistRepo;
        this.wishlistItemRepo = wishlistItemRepo;
        this.productRepo = productRepo;
    }

    // == Public Methods ==

    @Override
    DTOWishlistResponse addNewWish(UUID customerId, DTOWishlistRequest dto) {

        final Product product;

        try {

        } catch (NoSuchElementException e){
            throw centralAudit.audit(e,customerId, "addNewWish", AuditingStatus.WARNING, e.toString());
        }

        return null;
    }

    @Override
    Page<DTOWishlistResponse> findAllWishes(UUID customerId) {
        return null;
    }

    @Override
    DTOWishlistResponse findWish(UUID customerId, long wishId) {
        return null;
    }

    @Override
    void removeWish(UUID customerId, long wishId) {

    }

    @Override
    void clearWishlist(UUID customerId) {

    }

    // == Audit & throw ==

}
