package commerce.eshop.core.tests.application.customerTest.addons.address.queries;

import commerce.eshop.core.application.customer.addons.address.queries.AddressQueries;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.SortSanitizer;
import commerce.eshop.core.application.util.sort.CustomerAddrSort;
import commerce.eshop.core.model.entity.CustomerAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AddressQueriesTest {

    private DomainLookupService domainLookupService;
    private CentralAudit centralAudit;
    private SortSanitizer sortSanitizer;
    private AddressQueries addressQueries;

    @BeforeEach
    void setUp() {
        domainLookupService = mock(DomainLookupService.class);
        centralAudit = mock(CentralAudit.class);
        sortSanitizer = mock(SortSanitizer.class);

        addressQueries = new AddressQueries(domainLookupService, centralAudit, sortSanitizer);
    }

    @Test
    void returnAllAddresses() {
        UUID customerId = UUID.randomUUID();
        Pageable incoming = mock(Pageable.class);
        Pageable sanitized = mock(Pageable.class);
        Page<CustomerAddress> result = mock(Page.class);

        when(sortSanitizer.sanitize(
                eq(incoming),
                eq(CustomerAddrSort.ALLOWED_SORTS),
                eq(CustomerAddrSort.MAX_PAGE_SIZE)
        )).thenReturn(sanitized);
        when(domainLookupService.getPagedCustomerAddresses(customerId, sanitized)).thenReturn(result);

        Page<CustomerAddress> pagedResults = addressQueries.returnAllAddresses(customerId, incoming);

        assertSame(pagedResults, result);

        verify(sortSanitizer, times(1)).sanitize(
                eq(incoming),
                eq(CustomerAddrSort.ALLOWED_SORTS),
                eq(CustomerAddrSort.MAX_PAGE_SIZE));

        verify(domainLookupService, times(1)).getPagedCustomerAddresses(customerId, sanitized);
        verifyNoInteractions(centralAudit);
    }
}