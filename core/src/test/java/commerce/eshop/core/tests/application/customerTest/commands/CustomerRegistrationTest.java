package commerce.eshop.core.tests.application.customerTest.commands;

import commerce.eshop.core.application.customer.commands.CustomerRegistration;
import commerce.eshop.core.application.customer.factory.CustomerFactory;
import commerce.eshop.core.application.customer.writer.CustomerWriter;
import commerce.eshop.core.application.events.customer.CustomerRegisteredEvent;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.web.dto.requests.Customer.DTOCustomerCreateUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CustomerRegistrationTest {

    // == Fields ==
    private CustomerFactory customerFactory;
    private CustomerWriter customerWriter;
    private ApplicationEventPublisher publisher;
    private CustomerRegistration customerRegistration;

    @BeforeEach
    void setUp() {
        customerFactory = mock(CustomerFactory.class);
        customerWriter = mock(CustomerWriter.class);
        publisher = mock(ApplicationEventPublisher.class);

        customerRegistration = new CustomerRegistration(customerFactory, customerWriter, publisher);
    }

    @Test
    void handle() {
        DTOCustomerCreateUser dto = mock(DTOCustomerCreateUser.class);
        UUID customerId = UUID.randomUUID();
        Customer customer = mock(Customer.class);
        Customer result = mock(Customer.class);

        when(customerFactory.createFrom(dto)).thenReturn(customer);
        when(result.getCustomerId()).thenReturn(customerId);
        when(customer.getCustomerId()).thenReturn(customerId);
        when(customerWriter.save(customer)).thenReturn(result);
        when(customer.getCustomerId()).thenReturn(customerId);

        Customer returned = customerRegistration.handle(dto);

        verify(customerFactory, times(1)).createFrom(dto);
        verify(customerWriter, times(1)).save(customer);
        assertSame(result, returned);
        ArgumentCaptor<CustomerRegisteredEvent> eventCaptor =
                ArgumentCaptor.forClass(CustomerRegisteredEvent.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        CustomerRegisteredEvent published = eventCaptor.getValue();
        assertEquals(result.getCustomerId(), published.customerId());

        // No more interactions
        verifyNoMoreInteractions(customerFactory, customerWriter, publisher);
    }
}