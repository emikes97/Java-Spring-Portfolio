package commerce.eshop.core.application.customer.address.queries;

import commerce.eshop.core.model.entity.CustomerAddress;
import commerce.eshop.core.service.DomainLookupService;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.SortSanitizer;
import commerce.eshop.core.util.sort.CustomerAddrSort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class AddressQueries {

    // == Fields ==
    private final DomainLookupService domainLookupService;
    private final CentralAudit centralAudit;
    private final SortSanitizer sortSanitizer;

    // == Constructors ==
    @Autowired
    public AddressQueries(DomainLookupService domainLookupService, CentralAudit centralAudit, SortSanitizer sortSanitizer) {
        this.domainLookupService = domainLookupService;
        this.centralAudit = centralAudit;
        this.sortSanitizer = sortSanitizer;
    }

    // == Public Methods ==
    @Transactional(readOnly = true)
    public Page<CustomerAddress> returnAllAddresses(UUID customerId, Pageable pageable){
        Pageable p = sortSanitizer.sanitize(pageable, CustomerAddrSort.ALLOWED_SORTS, CustomerAddrSort.MAX_PAGE_SIZE);
        return domainLookupService.getPagedCustomerAddresses(customerId, p);
    }
}
