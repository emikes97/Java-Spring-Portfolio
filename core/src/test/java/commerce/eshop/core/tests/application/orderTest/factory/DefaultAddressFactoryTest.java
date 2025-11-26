package commerce.eshop.core.tests.application.orderTest.factory;

import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.order.factory.DefaultAddressFactory;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.model.entity.CustomerAddress;
import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultAddressFactoryTest {

    // == Fields ==
    private DomainLookupService domainLookupService;
    private DefaultAddressFactory defaultAddressFactory;

    @BeforeEach
    void setUp() {
        domainLookupService = mock(DomainLookupService.class);

        defaultAddressFactory = new DefaultAddressFactory(domainLookupService);
    }

    @Test
    void handle_success() {
        UUID customerId = UUID.randomUUID();
        CustomerAddress address = mock(CustomerAddress.class);

        when(address.getCountry()).thenReturn("Greece");
        when(address.getStreet()).thenReturn("Athinas 123");
        when(address.getCity()).thenReturn("Athens");
        when(address.getPostalCode()).thenReturn("10551");
        when(domainLookupService.getCustomerAddrOrThrow(customerId, EndpointsNameMethods.ORDER_PLACE))
                .thenReturn(address);

        DTOOrderCustomerAddress dto = defaultAddressFactory.handle(customerId);

        assertNotNull(dto);
        assertEquals("Greece", dto.country());
        assertEquals("Athinas 123", dto.street());
        assertEquals("Athens", dto.city());
        assertEquals("10551", dto.postalCode());

        verify(domainLookupService, times(1))
                .getCustomerAddrOrThrow(customerId, EndpointsNameMethods.ORDER_PLACE);
    }
}