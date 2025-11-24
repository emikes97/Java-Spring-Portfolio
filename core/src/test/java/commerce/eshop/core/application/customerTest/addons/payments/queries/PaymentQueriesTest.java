package commerce.eshop.core.application.customerTest.addons.payments.queries;

import commerce.eshop.core.application.customer.addons.payments.queries.PaymentQueries;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.util.SortSanitizer;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.sort.CustomerPaymentMethodSort;
import commerce.eshop.core.model.entity.CustomerPaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PaymentQueriesTest {

    private DomainLookupService domainLookupService;
    private SortSanitizer sortSanitizer;
    private PaymentQueries paymentQueries;

    @BeforeEach
    void setUp() {
        domainLookupService = mock(DomainLookupService.class);
        sortSanitizer = mock(SortSanitizer.class);

        paymentQueries = new PaymentQueries(domainLookupService, sortSanitizer);
    }

    @Test
    void getPagedPaymentMethods() {
        UUID customerId = UUID.randomUUID();
        Pageable incoming = mock(Pageable.class);
        Pageable sanitized = mock(Pageable.class);
        Page<CustomerPaymentMethod> paged = mock(Page.class);

        when(sortSanitizer.sanitize(
                eq(incoming),
                eq(CustomerPaymentMethodSort.PAYMENT_METHOD_SORT_WHITELIST),
                eq(CustomerPaymentMethodSort.MAX_PAGE_SIZE)
        )).thenReturn(sanitized);
        when(domainLookupService.getPagedPaymentMethods(customerId, sanitized)).thenReturn(paged);

        Page<CustomerPaymentMethod> result = paymentQueries.getPagedPaymentMethods(customerId, incoming);

        assertSame(paged, result);
        verify(sortSanitizer).sanitize(
                eq(incoming),
                eq(CustomerPaymentMethodSort.PAYMENT_METHOD_SORT_WHITELIST),
                eq(CustomerPaymentMethodSort.MAX_PAGE_SIZE)
        );
        verify(domainLookupService).getPagedPaymentMethods(customerId, sanitized);
    }

    @Test
    void retrievePaymentMethod() {
        UUID customerId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        CustomerPaymentMethod paymentMethod = mock(CustomerPaymentMethod.class);

        when(domainLookupService.getPaymentMethodOrThrow(customerId, paymentId, EndpointsNameMethods.PM_RETRIEVE)).thenReturn(paymentMethod);

        CustomerPaymentMethod result = paymentQueries.retrievePaymentMethod(customerId, paymentId);

        assertSame(paymentMethod, result);
        verify(domainLookupService).getPaymentMethodOrThrow(customerId, paymentId, EndpointsNameMethods.PM_RETRIEVE);
    }
}