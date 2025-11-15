package commerce.eshop.core.application.customer.addons.payments.queries;

import commerce.eshop.core.model.entity.CustomerPaymentMethod;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.util.SortSanitizer;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.sort.CustomerPaymentMethodSort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class PaymentQueries {

    // == Fields ==
    private final DomainLookupService domainLookupService;
    private final SortSanitizer sortSanitizer;

    // == Constructors ==
    @Autowired
    public PaymentQueries(DomainLookupService domainLookupService, SortSanitizer sortSanitizer) {
        this.domainLookupService = domainLookupService;
        this.sortSanitizer = sortSanitizer;
    }

    // == Public Methods ==
    @Transactional(readOnly = true)
    public Page<CustomerPaymentMethod> getPagedPaymentMethods(UUID customerId, Pageable pageable){
        Pageable p = sortSanitizer.sanitize(pageable, CustomerPaymentMethodSort.PAYMENT_METHOD_SORT_WHITELIST, CustomerPaymentMethodSort.MAX_PAGE_SIZE);
        return domainLookupService.getPagedPaymentMethods(customerId, p);
    }

    @Transactional(readOnly = true)
    public CustomerPaymentMethod retrievePaymentMethod(UUID customerId, UUID paymentMethodId){
        return domainLookupService.getPaymentMethodOrThrow(customerId, paymentMethodId, EndpointsNameMethods.PM_RETRIEVE);
    }
}
