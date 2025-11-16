package commerce.eshop.core.application.customer.queries;

import commerce.eshop.core.application.customer.validation.AuditedCustomerValidation;
import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.CartItem;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.util.SortSanitizer;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.sort.CustomerSort;
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
    private final AuditedCustomerValidation validation;

    // == Constructors ==
    @Autowired
    public CustomerQueries(DomainLookupService domainLookupService, SortSanitizer sortSanitizer, AuditedCustomerValidation validation) {
        this.domainLookupService = domainLookupService;
        this.sortSanitizer = sortSanitizer;
        this.validation = validation;
    }

    // == Public methods ==

    @Transactional(readOnly = true)
    public Customer getCustomerProfile(UUID customerId){
        validation.verifyCustomer(customerId, EndpointsNameMethods.GET_PROFILE_BY_ID);
        return domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.GET_PROFILE_BY_ID);
    }

    @Transactional(readOnly = true)
    public Customer getCustomerProfile(String phoneOrEmail){
        validation.requireNotBlank(phoneOrEmail, null, EndpointsNameMethods.GET_PROFILE_BY_SEARCH,
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
}

