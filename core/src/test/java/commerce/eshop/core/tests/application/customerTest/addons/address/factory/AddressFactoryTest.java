package commerce.eshop.core.tests.application.customerTest.addons.address.factory;

import commerce.eshop.core.application.customer.addons.address.factory.AddressFactory;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.CustomerAddress;
import commerce.eshop.core.web.dto.requests.CustomerAddr.DTOAddCustomerAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AddressFactoryTest {

    private AddressFactory addressFactory;

    @BeforeEach
    void setUp() {
        addressFactory = new AddressFactory();
    }

    @Test
    void handle() {
        Customer customer = mock(Customer.class);
        UUID customerId = UUID.randomUUID();

        when(customer.getCustomerId()).thenReturn(customerId);

        DTOAddCustomerAddress dto = new DTOAddCustomerAddress(
                "Greece",
                "Acharnon 55",
                "Athens",
                "10433",
                true
        );

        CustomerAddress address = addressFactory.handle(dto, customer);

        assertNotNull(address);
        assertSame(customer, address.getCustomer());
        assertEquals("Greece", address.getCountry());
        assertEquals("Acharnon 55", address.getStreet());
        assertEquals("Athens", address.getCity());
        assertEquals("10433", address.getPostalCode());
        assertTrue(address.isDefault());
    }
}