package commerce.eshop.core.service.Impl;

import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.model.entity.Wishlist;
import commerce.eshop.core.model.entity.WishlistItem;
import commerce.eshop.core.repository.ProductRepo;
import commerce.eshop.core.repository.WishlistItemRepo;
import commerce.eshop.core.repository.WishlistRepo;
import commerce.eshop.core.service.DomainLookupService;
import commerce.eshop.core.service.WishlistService;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.SortSanitizer;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditMessage;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.util.sort.WishlistSort;
import commerce.eshop.core.web.dto.response.Wishlist.DTOWishlistResponse;
import commerce.eshop.core.web.mapper.WishlistServiceMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class WishlistServiceImpl implements WishlistService {

    // == Fields ==
    private final CentralAudit centralAudit;
    private final WishlistItemRepo wishlistItemRepo;
    private final WishlistRepo wishlistRepo;
    private final ProductRepo productRepo;
    private final SortSanitizer sortSanitizer;
    private final WishlistServiceMapper wishlistServiceMapper;
    private final DomainLookupService domainLookupService;

    // == Constructors ==
    public WishlistServiceImpl(CentralAudit centralAudit, WishlistRepo wishlistRepo, WishlistItemRepo wishlistItemRepo,
                               ProductRepo productRepo, SortSanitizer sortSanitizer, WishlistServiceMapper wishlistServiceMapper,
                               DomainLookupService domainLookupService){

        this.centralAudit = centralAudit;
        this.wishlistRepo = wishlistRepo;
        this.wishlistItemRepo = wishlistItemRepo;
        this.productRepo = productRepo;
        this.sortSanitizer = sortSanitizer;
        this.wishlistServiceMapper = wishlistServiceMapper;
        this.domainLookupService = domainLookupService;
    }

    // == Public Methods ==

    @Transactional
    @Override
    public DTOWishlistResponse addNewWish(UUID customerId, long productId) {

        final Product product = domainLookupService.getProductOrThrow(customerId, productId, EndpointsNameMethods.ADD_NEW_WISH);

        final Wishlist wishlist = domainLookupService.getWishlistOrThrow(customerId, EndpointsNameMethods.ADD_NEW_WISH);

        final WishlistItem wishlistItem = new WishlistItem(wishlist, product, product.getProductName());

        try {
            wishlistItemRepo.saveAndFlush(wishlistItem);
            centralAudit.info(customerId, EndpointsNameMethods.ADD_NEW_WISH, AuditingStatus.SUCCESSFUL,
                    AuditMessage.WISHLIST_ADD_NEW_WISH_SUCCESS.getMessage());
            return wishlistServiceMapper.toDto(wishlistItem);
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, customerId, EndpointsNameMethods.ADD_NEW_WISH, AuditingStatus.ERROR, dup.toString());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Page<DTOWishlistResponse> findAllWishes(UUID customerId, Pageable pageable) {
        Pageable p = sortSanitizer.sanitize(pageable, WishlistSort.WISHLIST_SORT_WHITELIST, WishlistSort.MAX_PAGE_SIZE);

        final Wishlist wishlist = domainLookupService.getWishlistOrThrow(customerId, EndpointsNameMethods.FIND_ALL_WISHES);

        Page<WishlistItem> items = wishlistItemRepo.findByWishlist_WishlistId(wishlist.getWishlistId(), p);

        centralAudit.info(customerId, EndpointsNameMethods.FIND_ALL_WISHES, AuditingStatus.SUCCESSFUL,
                AuditMessage.WISHLIST_FIND_ALL_WISHES_SUCCESS.getMessage());
        return items.map(wishlistServiceMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public DTOWishlistResponse findWish(UUID customerId, long wishId) {

        final Wishlist wishlist = domainLookupService.getWishlistOrThrow(customerId, EndpointsNameMethods.FIND_WISH);

        final WishlistItem wishlistItem = domainLookupService.getWishOrThrow(customerId, wishlist, wishId, EndpointsNameMethods.FIND_WISH);

        centralAudit.info(customerId, EndpointsNameMethods.FIND_WISH, AuditingStatus.SUCCESSFUL,
                AuditMessage.WISHLIST_FIND_A_WISH_SUCCESS.getMessage());
        return wishlistServiceMapper.toDto(wishlistItem);
    }

    @Transactional
    @Override
    public void removeWish(UUID customerId, long wishId) {

        final Wishlist wishlist = domainLookupService.getWishlistOrThrow(customerId, EndpointsNameMethods.REMOVE_WISH);

        final WishlistItem wishlistItem = domainLookupService.getWishOrThrow(customerId, wishlist, wishId, EndpointsNameMethods.REMOVE_WISH);

        try {
            wishlistItemRepo.delete(wishlistItem);
            wishlistItemRepo.flush();
            centralAudit.info(customerId, EndpointsNameMethods.REMOVE_WISH, AuditingStatus.SUCCESSFUL,
                    AuditMessage.WISHLIST_REMOVE_A_WISH_SUCCESS.getMessage());
        }catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, customerId, EndpointsNameMethods.REMOVE_WISH, AuditingStatus.ERROR, dup.toString());
        }
    }

    @Transactional
    @Override
    public void clearWishlist(UUID customerId) {

        final Wishlist wishlist = domainLookupService.getWishlistOrThrow(customerId, EndpointsNameMethods.CLEAR_WISHLIST);

        try {
            int expected = wishlistItemRepo.countWishlistItems(wishlist.getWishlistId());
            int updated  = wishlistItemRepo.clearWishlist(wishlist.getWishlistId());

            if(updated != expected){
                throw centralAudit.audit(new IllegalStateException("Data mismatch"), customerId, EndpointsNameMethods.CLEAR_WISHLIST,
                        AuditingStatus.ERROR, "DATA_MISMATCH");
            }

            wishlistItemRepo.flush();
            centralAudit.info(customerId, EndpointsNameMethods.CLEAR_WISHLIST, AuditingStatus.SUCCESSFUL,
                    AuditMessage.WISHLIST_CLEARED_SUCCESS.getMessage());

        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, customerId, EndpointsNameMethods.CLEAR_WISHLIST, AuditingStatus.ERROR, dup.toString());
        }
    }
}
