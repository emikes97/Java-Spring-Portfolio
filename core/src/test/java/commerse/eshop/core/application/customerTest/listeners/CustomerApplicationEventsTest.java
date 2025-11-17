package commerse.eshop.core.application.customerTest.listeners;

import commerce.eshop.core.application.customer.listeners.CustomerApplicationEvents;
import commerce.eshop.core.application.email.EmailComposer;
import commerce.eshop.core.application.events.customer.CustomerFailedUpdatePasswordEvent;
import commerce.eshop.core.application.events.customer.CustomerRegisteredEvent;
import commerce.eshop.core.application.events.customer.CustomerSuccessfulOrFailedUpdatePasswordEvent;
import commerce.eshop.core.application.events.customer.CustomerUpdatedInfoEvent;
import commerce.eshop.core.application.events.email.EmailEventRequest;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.model.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerApplicationEventsTest {

    private EmailComposer emailComposer;
    private ApplicationEventPublisher publisher;
    private DomainLookupService domainLookupService;
    private CustomerApplicationEvents customerApplicationEvents;

    @BeforeEach
    void setUp() {
        emailComposer = mock(EmailComposer.class);
        publisher = mock(ApplicationEventPublisher.class);
        domainLookupService = mock(DomainLookupService.class);

        customerApplicationEvents = new CustomerApplicationEvents(emailComposer, publisher, domainLookupService);
    }

    @Test
    void testOn_customerRegisteredEvent_fired() {
        CustomerRegisteredEvent event = mock(CustomerRegisteredEvent.class);
        UUID customerId = UUID.randomUUID();
        Customer customer = mock(Customer.class);
        EmailEventRequest publishMailEvent = mock(EmailEventRequest.class);

        when(event.customerId()).thenReturn(customerId);
        when(domainLookupService.getCustomerOrThrow(eq(customerId), anyString())).thenReturn(customer);
        when(emailComposer.accountCreated(eq(customer), anyString())).thenReturn(publishMailEvent);

        customerApplicationEvents.on(event);
        // verify lookup
        verify(domainLookupService).getCustomerOrThrow(eq(customerId), anyString());

        // verify composer call
        verify(emailComposer).accountCreated(eq(customer), anyString());

        // verify the SAME EmailEventRequest is published
        ArgumentCaptor<EmailEventRequest> captor = ArgumentCaptor.forClass(EmailEventRequest.class);
        verify(publisher).publishEvent(captor.capture());
        assertSame(publishMailEvent, captor.getValue());
        verifyNoMoreInteractions(domainLookupService, emailComposer, publisher);
    }

    @Test
    void testOn_customerRegisteredEvent_notFired() {
        UUID customerId = UUID.randomUUID();
        CustomerRegisteredEvent event = mock(CustomerRegisteredEvent.class);

        when(event.customerId()).thenReturn(customerId);
        when(domainLookupService.getCustomerOrThrow(eq(customerId), anyString())).thenThrow(new NoSuchElementException("not found"));

        customerApplicationEvents.on(event);

        verify(domainLookupService).getCustomerOrThrow(eq(customerId), anyString());
        verify(emailComposer, never()).accountCreated(any(), anyString());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void testOn_CustomerUpdatedInfoEvent_fired() {
        UUID customerId = UUID.randomUUID();
        Customer customer = mock(Customer.class);
        CustomerUpdatedInfoEvent event = mock(CustomerUpdatedInfoEvent.class);
        EmailEventRequest publishMailEvent = mock(EmailEventRequest.class);

        when(event.customerId()).thenReturn(customerId);
        when(domainLookupService.getCustomerOrThrow(eq(customerId), anyString())).thenReturn(customer);
        when(emailComposer.accountUpdated(eq(customer), any())).thenReturn(publishMailEvent);

        customerApplicationEvents.on(event);
        // verify lookup
        verify(domainLookupService).getCustomerOrThrow(eq(customerId), anyString());

        // verify composer call
        verify(emailComposer).accountUpdated(eq(customer), any());

        // verify the SAME EmailEventRequest is published
        ArgumentCaptor<EmailEventRequest> captor = ArgumentCaptor.forClass(EmailEventRequest.class);
        verify(publisher).publishEvent(captor.capture());
        assertSame(publishMailEvent, captor.getValue());
        verifyNoMoreInteractions(domainLookupService, emailComposer, publisher);
    }

    @Test
    void testOn_CustomerUpdatedInfoEvent_notFired(){
        UUID customerId = UUID.randomUUID();
        CustomerUpdatedInfoEvent event = mock(CustomerUpdatedInfoEvent.class);

        when(event.customerId()).thenReturn(customerId);
        when(domainLookupService.getCustomerOrThrow(eq(customerId), anyString())).thenThrow(new NoSuchElementException("not found"));

        assertThrows(NoSuchElementException.class, () -> customerApplicationEvents.on(event));

        verify(domainLookupService).getCustomerOrThrow(eq(customerId), anyString());
        verify(emailComposer, never()).accountUpdated(any(), anyString());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void testOn_CustomerFailedUpdatePasswordEvent_fired(){
        UUID customerId = UUID.randomUUID();
        Customer customer = mock(Customer.class);
        CustomerSuccessfulOrFailedUpdatePasswordEvent event = mock(CustomerSuccessfulOrFailedUpdatePasswordEvent.class);
        EmailEventRequest publishMailEvent = mock(EmailEventRequest.class);

        when(event.customerId()).thenReturn(customerId);
        when(event.successfulOrNot()).thenReturn(false);
        when(domainLookupService.getCustomerOrThrow(eq(customerId), anyString())).thenReturn(customer);
        when(emailComposer.passwordUpdated(eq(customer), anyBoolean())).thenReturn(publishMailEvent);

        customerApplicationEvents.on(event);

        verify(domainLookupService, times(1)).getCustomerOrThrow(eq(customerId), anyString());
        verify(emailComposer).passwordUpdated(eq(customer), anyBoolean());
        ArgumentCaptor<EmailEventRequest> captor = ArgumentCaptor.forClass(EmailEventRequest.class);
        verify(publisher).publishEvent(captor.capture());
        assertSame(publishMailEvent, captor.getValue());
        verifyNoMoreInteractions(domainLookupService, emailComposer, publisher);
    }

    @Test
    void testOn_CustomerFailedUpdatePasswordEvent_notFired(){

    }
}