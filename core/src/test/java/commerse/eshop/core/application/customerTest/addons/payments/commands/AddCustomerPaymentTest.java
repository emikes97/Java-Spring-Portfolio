package commerse.eshop.core.application.customerTest.addons.payments.commands;

import commerce.eshop.core.application.customer.addons.payments.commands.AddCustomerPayment;
import commerce.eshop.core.application.customer.addons.payments.factory.PaymentMethodFactory;
import commerce.eshop.core.application.customer.addons.payments.writer.PaymentMethodWriter;
import commerce.eshop.core.application.events.customer.PaymentMethodCreatedEvent;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.CustomerPaymentMethod;
import commerce.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOAddPaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddCustomerPaymentTest {

    private DomainLookupService domainLookupService;
    private PaymentMethodWriter paymentMethodWriter;
    private PaymentMethodFactory paymentMethodFactory;
    private ApplicationEventPublisher applicationEventPublisher;
    private AddCustomerPayment addCustomerPayment;

    @BeforeEach
    void setUp() {
        domainLookupService = mock(DomainLookupService.class);
        paymentMethodWriter = mock(PaymentMethodWriter.class);
        paymentMethodFactory = mock(PaymentMethodFactory.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);

        addCustomerPayment = new AddCustomerPayment(domainLookupService, paymentMethodWriter, paymentMethodFactory, applicationEventPublisher);
    }

    @Test
    void handle_defaultMethodTrue_clearsDefaults_andPublishesEvent() {
        UUID customerId = UUID.randomUUID();
        UUID paymentMethodId = UUID.randomUUID();
        Customer customer = mock(Customer.class);
        CustomerPaymentMethod created = mock(CustomerPaymentMethod.class);
        CustomerPaymentMethod saved = mock(CustomerPaymentMethod.class);
        DTOAddPaymentMethod dto = mock(DTOAddPaymentMethod.class);

        when(dto.isDefault()).thenReturn(true);
        when(dto.provider()).thenReturn("VISA");
        when(saved.getCustomerPaymentId()).thenReturn(paymentMethodId);
        when(saved.getProvider()).thenReturn("VISA");
        when(domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.PM_ADD))
                .thenReturn(customer);
        when(paymentMethodFactory.create(dto, customer, true)).thenReturn(created);
        when(paymentMethodWriter.save(created, EndpointsNameMethods.PM_ADD)).thenReturn(saved);

        CustomerPaymentMethod result = addCustomerPayment.handle(customerId, dto);

        assertSame(saved, result);

        // Order matters: if default → must clear first
        verify(paymentMethodWriter).updateDefaultToFalse(customerId);
        verify(paymentMethodFactory).create(dto, customer, true);
        verify(paymentMethodWriter).save(created, EndpointsNameMethods.PM_ADD);

        // Capture event
        ArgumentCaptor<PaymentMethodCreatedEvent> captor =
                ArgumentCaptor.forClass(PaymentMethodCreatedEvent.class);

        verify(applicationEventPublisher).publishEvent(captor.capture());

        PaymentMethodCreatedEvent event = captor.getValue();

        // Validate event data
        assertSame(paymentMethodId, event.paymentId());
        assertSame(customerId, event.customerId());
        assertSame("VISA", event.provider());
    }

    @Test
    void handle_defaultMethodFalse_doesNotClearDefaults_andPublishesEvent() {
        UUID customerId = UUID.randomUUID();
        UUID paymentMethodId = UUID.randomUUID();
        DTOAddPaymentMethod dto = mock(DTOAddPaymentMethod.class);
        Customer customer = mock(Customer.class);
        CustomerPaymentMethod created = mock(CustomerPaymentMethod.class);
        CustomerPaymentMethod saved = mock(CustomerPaymentMethod.class);

        when(dto.isDefault()).thenReturn(false);
        when(dto.provider()).thenReturn("VISA");
        when(saved.getCustomerPaymentId()).thenReturn(paymentMethodId);
        when(saved.getProvider()).thenReturn("VISA");
        when(domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.PM_ADD))
                .thenReturn(customer);
        when(paymentMethodFactory.create(dto, customer, false)).thenReturn(created);
        when(paymentMethodWriter.save(created, EndpointsNameMethods.PM_ADD)).thenReturn(saved);

        CustomerPaymentMethod result = addCustomerPayment.handle(customerId, dto);

        assertSame(saved, result);

        verify(paymentMethodWriter, never()).updateDefaultToFalse(any());
        verify(paymentMethodFactory).create(dto, customer, false);
        verify(paymentMethodWriter).save(created, EndpointsNameMethods.PM_ADD);

        ArgumentCaptor<PaymentMethodCreatedEvent> captor =
                ArgumentCaptor.forClass(PaymentMethodCreatedEvent.class);

        verify(applicationEventPublisher).publishEvent(captor.capture());

        PaymentMethodCreatedEvent event = captor.getValue();
        assertSame(paymentMethodId, event.paymentId());
        assertSame(customerId, event.customerId());
        assertSame("VISA", event.provider());
    }

    @Test
    void handle_customerNotFound_throws_andDoesNotContinue() {
        UUID customerId = UUID.randomUUID();
        DTOAddPaymentMethod dto = mock(DTOAddPaymentMethod.class);

        NoSuchElementException ex = new NoSuchElementException("not found");

        when(domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.PM_ADD))
                .thenThrow(ex);

        NoSuchElementException thrown = assertThrows(
                NoSuchElementException.class,
                () -> addCustomerPayment.handle(customerId, dto)
        );

        assertSame(ex, thrown);

        // NOTHING else should run
        verify(paymentMethodWriter, never()).updateDefaultToFalse(any());
        verify(paymentMethodFactory, never()).create(any(), any(), anyBoolean());
        verify(paymentMethodWriter, never()).save(any(), anyString());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }
}