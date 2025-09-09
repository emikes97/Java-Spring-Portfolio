package commerce.eshop.core.service.Impl;

import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.model.entity.Wishlist;
import commerce.eshop.core.model.entity.WishlistItem;
import commerce.eshop.core.repository.ProductRepo;
import commerce.eshop.core.repository.WishlistItemRepo;
import commerce.eshop.core.repository.WishlistRepo;
import commerce.eshop.core.service.WishlistService;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.SortSanitizer;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditMessage;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.web.dto.response.Wishlist.DTOWishlistResponse;
import commerce.eshop.core.web.mapper.WishlistServiceMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
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

    // == Whitelist & Constraints ==
    private static final Map<String, String> WISHLIST_SORT_WHITELIST = Map.ofEntries(
            Map.entry("productName", "productName"),
            Map.entry("product_name", "productName"),
            Map.entry("name", "productName"),
            Map.entry("addedAt", "addedAt"),
            Map.entry("added_at", "addedAt"),
            Map.entry("date", "addedAt")
    );

    // == Constructors ==
    public WishlistServiceImpl(CentralAudit centralAudit, WishlistRepo wishlistRepo, WishlistItemRepo wishlistItemRepo,
                               ProductRepo productRepo, SortSanitizer sortSanitizer, WishlistServiceMapper wishlistServiceMapper){

        this.centralAudit = centralAudit;
        this.wishlistRepo = wishlistRepo;
        this.wishlistItemRepo = wishlistItemRepo;
        this.productRepo = productRepo;
        this.sortSanitizer = sortSanitizer;
        this.wishlistServiceMapper = wishlistServiceMapper;
    }

    // == Public Methods ==

    @Transactional
    @Override
    public DTOWishlistResponse addNewWish(UUID customerId, long productId) {

        final Product product = getProductOrThrow(customerId, productId, EndpointsNameMethods.ADD_NEW_WISH);

        final Wishlist wishlist = getWishlistOrThrow(customerId, EndpointsNameMethods.ADD_NEW_WISH);

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
        Pageable p = sortSanitizer.sanitize(pageable, WISHLIST_SORT_WHITELIST, 25);

        final Wishlist wishlist = getWishlistOrThrow(customerId, EndpointsNameMethods.FIND_ALL_WISHES);

        Page<WishlistItem> items = wishlistItemRepo.findByWishlist_WishlistId(wishlist.getWishlistId(), p);

        centralAudit.info(customerId, EndpointsNameMethods.FIND_ALL_WISHES, AuditingStatus.SUCCESSFUL,
                AuditMessage.WISHLIST_FIND_ALL_WISHES_SUCCESS.getMessage());
        return items.map(wishlistServiceMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public DTOWishlistResponse findWish(UUID customerId, long wishId) {

        final Wishlist wishlist = getWishlistOrThrow(customerId, EndpointsNameMethods.FIND_WISH);

        final WishlistItem wishlistItem = getWishOrThrow(customerId, wishlist, wishId, EndpointsNameMethods.FIND_WISH);

        centralAudit.info(customerId, EndpointsNameMethods.FIND_WISH, AuditingStatus.SUCCESSFUL,
                AuditMessage.WISHLIST_FIND_A_WISH_SUCCESS.getMessage());
        return wishlistServiceMapper.toDto(wishlistItem);
    }

    @Transactional
    @Override
    public void removeWish(UUID customerId, long wishId) {

        final Wishlist wishlist = getWishlistOrThrow(customerId, EndpointsNameMethods.REMOVE_WISH);

        final WishlistItem wishlistItem = getWishOrThrow(customerId, wishlist, wishId, EndpointsNameMethods.REMOVE_WISH);

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

        final Wishlist wishlist = getWishlistOrThrow(customerId, EndpointsNameMethods.CLEAR_WISHLIST);

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

    // == Private Methods ==

    private Wishlist getWishlistOrThrow(UUID customerId, String method){
        try {
            final Wishlist wishlist = wishlistRepo.findWishlistByCustomerId(customerId).orElseThrow(
                    () -> new NoSuchElementException("NOT_FOUND_BY_ID")
            );
            return wishlist;
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.WARNING, e.toString());
        }
    }

    private Product getProductOrThrow(UUID customerId, long productId, String method){
        try {
           final Product product = productRepo.findById(productId).orElseThrow(
                    () -> new NoSuchElementException("The provided ID doesn't match with any available product.")
            );
           return product;
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e,customerId, method, AuditingStatus.WARNING, e.toString());
        }
    }

    private WishlistItem getWishOrThrow(UUID customerId, Wishlist wishlist, long wishId, String method){
        try {
            final WishlistItem wishlistItem = wishlistItemRepo.findWish(wishlist.getWishlistId(), wishId).orElseThrow(
                    () -> new NoSuchElementException("WISHLISTED_ITEM_NOT_FOUND")
            );
            return wishlistItem;
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, customerId, method, AuditingStatus.WARNING, e.toString());
        }
    }
}
