package commerse.eshop.core.application.customerTest.queries;

import commerce.eshop.core.application.customer.queries.CustomerQueries;
import commerce.eshop.core.application.customer.validation.AuditedCustomerValidation;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.infrastructure.impl.DomainLookupServiceImpl;
import commerce.eshop.core.application.util.SortSanitizer;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.sort.CustomerSort;
import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.CartItem;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerQueriesTest {

    // == Fields ==
    private DomainLookupService domainLookupService;
    private SortSanitizer sortSanitizer;
    private AuditedCustomerValidation validation;
    private CustomerQueries queries;

    @BeforeEach
    void setUp() {
        domainLookupService = mock(DomainLookupService.class);
        sortSanitizer = mock(SortSanitizer.class);
        validation = mock(AuditedCustomerValidation.class);
        queries = new CustomerQueries(domainLookupService, sortSanitizer, validation);
    }

    @Test
    void getCustomerProfile() {
        UUID customerId = UUID.randomUUID();
        Customer mockCustomer = mock(Customer.class);

        when(domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.GET_PROFILE_BY_ID)).thenReturn(mockCustomer);

        Customer result = queries.getCustomerProfile(customerId);
        assertSame(mockCustomer, result);

        verify(validation, times(1)).verifyCustomer(customerId, EndpointsNameMethods.GET_PROFILE_BY_ID);
        verify(domainLookupService, times(1)).getCustomerOrThrow(customerId, EndpointsNameMethods.GET_PROFILE_BY_ID);
        verifyNoMoreInteractions(validation, domainLookupService);
    }

    @Test
    void getCustomerProfile_fail(){
        UUID customerId = UUID.randomUUID();

        IllegalArgumentException ex = new IllegalArgumentException("welp");

        doThrow(ex).when(validation).verifyCustomer(customerId, EndpointsNameMethods.GET_PROFILE_BY_ID);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> validation.verifyCustomer(customerId, EndpointsNameMethods.GET_PROFILE_BY_ID));

        assertSame(ex, thrown);
        verify(validation, times(1)).verifyCustomer(customerId, EndpointsNameMethods.GET_PROFILE_BY_ID);
        verifyNoInteractions(domainLookupService);
    }

    @Test
    void testGetCustomerProfile() {
        String phoneOrMail = "  jotaro@dio.com ";
        String trimmed = "jotaro@dio.com";
        Customer customer = mock(Customer.class);

        when(domainLookupService.getCustomerByPhoneOrEmailOrThrow(trimmed, EndpointsNameMethods.GET_PROFILE_BY_SEARCH)).thenReturn(customer);

        // validation passes as no empty

        Customer result = queries.getCustomerProfile(phoneOrMail);
        assertSame(result, customer);
        verify(validation, times(1)).requireNotBlank(phoneOrMail, null, EndpointsNameMethods.GET_PROFILE_BY_SEARCH,
                "MISSING_IDENTIFIER", "Missing phone/email identifier.");
        verify(domainLookupService, times(1)).getCustomerByPhoneOrEmailOrThrow(trimmed, EndpointsNameMethods.GET_PROFILE_BY_SEARCH);
        verifyNoMoreInteractions(validation, domainLookupService);
    }

    @Test
    void testGetCustomerProfile_fail(){
        String phoneOrMail = " ";

        IllegalArgumentException ex = new IllegalArgumentException("Dioooooo");
        doThrow(ex).when(validation).requireNotBlank(phoneOrMail, null, EndpointsNameMethods.GET_PROFILE_BY_SEARCH,
                "MISSING_IDENTIFIER", "Missing phone/email identifier.");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> queries.getCustomerProfile(phoneOrMail));

        assertSame(ex, thrown);
        verify(validation, times(1)).requireNotBlank(phoneOrMail, null, EndpointsNameMethods.GET_PROFILE_BY_SEARCH,
                "MISSING_IDENTIFIER", "Missing phone/email identifier.");
        verifyNoInteractions(domainLookupService);
        verifyNoMoreInteractions(validation);
    }

    @Test
    void getCustomerOrders() {
        UUID customerId = UUID.randomUUID();
        Pageable original = mock(Pageable.class);
        Pageable sanitized = mock(Pageable.class);

        Page<Order> page = mock(Page.class);

        when(sortSanitizer.sanitize(
                original,
                CustomerSort.CUSTOMER_ORDERS_SORT_WHITELIST,
                CustomerSort.MAX_PAGE_SIZE
        )).thenReturn(sanitized);

        when(domainLookupService.getPagedOrders(customerId, sanitized)).thenReturn(page);

        Page<Order> result = queries.getCustomerOrders(customerId, original);

        assertSame(page, result);

        verify(sortSanitizer).sanitize(
                original,
                CustomerSort.CUSTOMER_ORDERS_SORT_WHITELIST,
                CustomerSort.MAX_PAGE_SIZE
        );

        verify(domainLookupService).getPagedOrders(customerId, sanitized);
        verifyNoMoreInteractions(sortSanitizer, domainLookupService);
    }

    @Test
    void getCustomerCartItems() {
        UUID customerId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        Cart cart = mock(Cart.class);
        Pageable original = mock(Pageable.class);
        Pageable sanitized = mock(Pageable.class);
        @SuppressWarnings("unchecked")
        Page<CartItem> page = mock(Page.class);

        when(cart.getCartId()).thenReturn(cartId);
        when(domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.GET_CART_ITEMS)).thenReturn(cart);
        when(sortSanitizer.sanitize(original, CustomerSort.CUSTOMER_CART_ITEMS_SORT_WHITELIST, CustomerSort.MAX_PAGE_SIZE)).thenReturn(sanitized);
        when(domainLookupService.getPagedCartItems(cart.getCartId(), sanitized)).thenReturn(page);

        Page<CartItem> result = queries.getCustomerCartItems(customerId, original);
        assertSame(page, result);

        verify(domainLookupService).getCartOrThrow(customerId, EndpointsNameMethods.GET_CART_ITEMS);
        verify(sortSanitizer).sanitize(
                original,
                CustomerSort.CUSTOMER_CART_ITEMS_SORT_WHITELIST,
                CustomerSort.MAX_PAGE_SIZE
        );
        verify(domainLookupService).getPagedCartItems(cart.getCartId(), sanitized);
        verifyNoMoreInteractions(domainLookupService, sortSanitizer);
    }
}