package commerce.eshop.core.application.customer.queries;

import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.CartItem;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.service.DomainLookupService;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.SortSanitizer;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.util.sort.CustomerSort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class CustomerQueries {

    // == Fields ==
    private final DomainLookupService domainLookupService;
    private final SortSanitizer sortSanitizer;
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public CustomerQueries(DomainLookupService domainLookupService, CentralAudit centralAudit, SortSanitizer sortSanitizer) {
        this.domainLookupService = domainLookupService;
        this.centralAudit = centralAudit;
        this.sortSanitizer = sortSanitizer;
    }

    // == Public methods ==

    @Transactional(readOnly = true)
    public Customer getCustomerProfile(UUID customerId){

        if (customerId == null) {
            IllegalArgumentException bad = new IllegalArgumentException("Missing customerId.");
            throw centralAudit.audit(bad, null, EndpointsNameMethods.GET_PROFILE_BY_ID, AuditingStatus.WARNING, "MISSING_CUSTOMER_ID");
        }

        return domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.GET_PROFILE_BY_ID);
    }

    @Transactional(readOnly = true)
    public Customer getCustomerProfile(String phoneOrEmail){

        requireNotBlank(phoneOrEmail, null, EndpointsNameMethods.GET_PROFILE_BY_SEARCH,
                "MISSING_IDENTIFIER", "Missing phone/email identifier.");
        final String key = phoneOrEmail.trim();
        return domainLookupService.getCustomerByPhoneOrEmailOrThrow(key, EndpointsNameMethods.GET_PROFILE_BY_SEARCH);

    }

    @Transactional(readOnly = true)
    public Page<Order> getCustomerOrders(UUID customerId, Pageable pageable){
        Pageable p = sortSanitizer.sanitize(pageable, CustomerSort.CUSTOMER_ORDERS_SORT_WHITELIST, CustomerSort.MAX_PAGE_SIZE);
        return domainLookupService.getPagedOrders(customerId, p);
    }

    @Transactional(readOnly = true)
    public Page<CartItem> getCustomerCartItems(UUID customerId, Pageable pageable){
        final Cart cart = domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.GET_CART_ITEMS);
        Pageable p = sortSanitizer.sanitize(pageable, CustomerSort.CUSTOMER_CART_ITEMS_SORT_WHITELIST, CustomerSort.MAX_PAGE_SIZE);
        return domainLookupService.getPagedCartItems(cart.getCartId(), p);
    }

    // == Private methods ==

    /** Fail if the value is blank. Audits with WARNING and throws 400 (IllegalArgumentException). */
    private void requireNotBlank(String val, UUID cid, String endpoint, String code, String msg) {
        if (val == null || val.isBlank()) {
            throw centralAudit.audit(new IllegalArgumentException(msg), cid, endpoint, AuditingStatus.WARNING, code);
        }
    }
}

