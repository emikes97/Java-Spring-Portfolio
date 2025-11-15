package commerce.eshop.core.service.Impl;

import commerce.eshop.core.application.wishlist.commands.AddWish;
import commerce.eshop.core.application.wishlist.commands.RemoveWish;
import commerce.eshop.core.application.wishlist.queries.WishQueries;
import commerce.eshop.core.model.entity.WishlistItem;
import commerce.eshop.core.service.WishlistService;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditMessage;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.web.dto.response.Wishlist.DTOWishlistResponse;
import commerce.eshop.core.web.mapper.WishlistServiceMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WishlistServiceImpl implements WishlistService {

    // == Fields ==
    private final AddWish addWish;
    private final WishQueries wishQueries;
    private final RemoveWish removeWish;
    private final CentralAudit centralAudit;
    private final WishlistServiceMapper wishlistServiceMapper;

    // == Constructors ==
    public WishlistServiceImpl(AddWish addWish, WishQueries wishQueries, RemoveWish removeWish, CentralAudit centralAudit,
                               WishlistServiceMapper wishlistServiceMapper){
        this.addWish = addWish;
        this.wishQueries = wishQueries;
        this.removeWish = removeWish;
        this.centralAudit = centralAudit;
        this.wishlistServiceMapper = wishlistServiceMapper;
    }

    // == Public Methods ==

    @Override
    public DTOWishlistResponse addNewWish(UUID customerId, long productId) {
        final WishlistItem item = addWish.handle(customerId, productId);
        centralAudit.info(customerId, EndpointsNameMethods.ADD_NEW_WISH, AuditingStatus.SUCCESSFUL,
                AuditMessage.WISHLIST_ADD_NEW_WISH_SUCCESS.getMessage());
        return wishlistServiceMapper.toDto(item);
    }

    @Override
    public Page<DTOWishlistResponse> findAllWishes(UUID customerId, Pageable pageable) {
        Page<WishlistItem> wishes = wishQueries.getAllPagedWishlistItems(customerId, pageable);
        centralAudit.info(customerId, EndpointsNameMethods.FIND_ALL_WISHES, AuditingStatus.SUCCESSFUL,
                AuditMessage.WISHLIST_FIND_ALL_WISHES_SUCCESS.getMessage());
        return wishes.map(wishlistServiceMapper::toDto);
    }

    @Override
    public DTOWishlistResponse findWish(UUID customerId, long wishId) {
        WishlistItem item = wishQueries.getWishlistItem(customerId, wishId);
        centralAudit.info(customerId, EndpointsNameMethods.FIND_WISH, AuditingStatus.SUCCESSFUL,
                AuditMessage.WISHLIST_FIND_A_WISH_SUCCESS.getMessage());
        return wishlistServiceMapper.toDto(item);
    }

    @Override
    public void removeWish(UUID customerId, long wishId) {
        removeWish.handle(customerId, wishId);
        centralAudit.info(customerId, EndpointsNameMethods.REMOVE_WISH, AuditingStatus.SUCCESSFUL,
                AuditMessage.WISHLIST_REMOVE_A_WISH_SUCCESS.getMessage());
    }

    @Override
    public void clearWishlist(UUID customerId) {
        removeWish.handle(customerId);
        centralAudit.info(customerId, EndpointsNameMethods.CLEAR_WISHLIST, AuditingStatus.SUCCESSFUL,
                AuditMessage.WISHLIST_CLEARED_SUCCESS.getMessage());
    }
}
