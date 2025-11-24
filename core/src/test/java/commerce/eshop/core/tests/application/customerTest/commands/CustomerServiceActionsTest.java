package commerce.eshop.core.tests.application.customerTest.commands;

import commerce.eshop.core.application.customer.commands.CustomerServiceActions;
import commerce.eshop.core.application.customer.validation.AuditedCustomerValidation;
import commerce.eshop.core.application.customer.writer.CustomerWriter;
import commerce.eshop.core.application.events.customer.CustomerSuccessfulOrFailedUpdatePasswordEvent;
import commerce.eshop.core.application.events.customer.CustomerUpdatedInfoEvent;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.model.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomerServiceActionsTest {

    // == Fields ==
    private DomainLookupService domainLookupService;
    private AuditedCustomerValidation validator;
    private CustomerWriter customerWriter;
    private ApplicationEventPublisher publisher;
    private CustomerServiceActions customerServiceActions;

    @BeforeEach
    void setUp() {
        domainLookupService = mock(DomainLookupService.class);
        validator = mock(AuditedCustomerValidation.class);
        customerWriter = mock(CustomerWriter.class);
        publisher = mock(ApplicationEventPublisher.class);

        customerServiceActions = new CustomerServiceActions(domainLookupService, validator, customerWriter, publisher);
    }

    @Test
    void handleUpdateName() {
        UUID customerId = UUID.randomUUID();
        String password = "secret";
        String rawName = "  John  ";
        String trimmed = "John";
        Customer customer = mock(Customer.class);

        when(customer.getCustomerId()).thenReturn(customerId);
        when(domainLookupService.getCustomerOrThrow(eq(customerId), any())).thenReturn(customer);
        when(validator.hasNoUpdate(any(Customer.class), eq(trimmed), any()))
                .thenReturn(false);

        customerServiceActions.handleUpdateName(customerId, password, rawName);

        verify(validator).verifyCustomer(eq(customerId), any());
        verify(validator).requireNotBlank(eq(rawName), eq(customerId), any(),
                any(), any());
        verify(domainLookupService).getCustomerOrThrow(eq(customerId), any());
        verify(validator).verifyPasswordOrThrow(any(Customer.class), eq(password), eq(customerId), any());
        verify(validator).hasNoUpdate(any(Customer.class), eq(trimmed), any());

        verify(customer).setName(trimmed);
        verify(customerWriter).save(customer);

        ArgumentCaptor<CustomerUpdatedInfoEvent> eventCaptor =
                ArgumentCaptor.forClass(CustomerUpdatedInfoEvent.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        CustomerUpdatedInfoEvent published = eventCaptor.getValue();
        assertEquals(customerId, published.customerId());
        assertEquals("Name", published.changed());

        verifyNoMoreInteractions(domainLookupService, validator, customerWriter, publisher);
    }

    @Test
    void handleUpdateName_NoChange() {
        UUID customerId = UUID.randomUUID();
        String password = "secret";
        String rawName = "  John  ";
        String trimmed = "John";
        Customer customer = mock(Customer.class);

        when(customer.getCustomerId()).thenReturn(customerId);
        when(domainLookupService.getCustomerOrThrow(eq(customerId), any()))
                .thenReturn(customer);
        when(validator.hasNoUpdate(any(Customer.class), eq(trimmed), any()))
                .thenReturn(true); // <-- NO CHANGE

        customerServiceActions.handleUpdateName(customerId, password, rawName);

        verify(validator).verifyCustomer(eq(customerId), any());
        verify(validator).requireNotBlank(eq(rawName), eq(customerId), any(),
                any(), any());
        verify(domainLookupService).getCustomerOrThrow(eq(customerId), any());
        verify(validator).verifyPasswordOrThrow(any(Customer.class), eq(password), eq(customerId), any());
        verify(validator).hasNoUpdate(any(Customer.class), eq(trimmed), any());

        verify(customer, never()).setName(anyString());
        verify(customerWriter, never()).save(any());
        verify(publisher, never()).publishEvent(any());

        verifyNoMoreInteractions(domainLookupService, validator, customerWriter, publisher);
    }

    @Test
    void handleUpdateSurname() {
        UUID customerId = UUID.randomUUID();
        String password = "secret";
        String lastName = " Cena ";
        String trimmed = "Cena";
        Customer customer = mock(Customer.class);

        when(customer.getCustomerId()).thenReturn(customerId);
        when(domainLookupService.getCustomerOrThrow(eq(customerId), any())).thenReturn(customer);
        when(customer.getCustomerId()).thenReturn(customerId);
        when(validator.hasNoUpdate(any(Customer.class), eq(trimmed), any()))
                .thenReturn(false);

        customerServiceActions.handleUpdateSurname(customerId, password, lastName);

        verify(validator).verifyCustomer(eq(customerId), anyString());
        verify(validator).requireNotBlank(eq(lastName), eq(customerId), anyString(),
                anyString(), anyString());
        verify(domainLookupService).getCustomerOrThrow(eq(customerId), any());
        verify(validator).verifyPasswordOrThrow(any(Customer.class), eq(password), eq(customerId), anyString());
        verify(validator).hasNoUpdate(any(Customer.class), eq(trimmed), anyString());

        verify(customer).setSurname(trimmed);
        verify(customerWriter).save(customer);

        ArgumentCaptor<CustomerUpdatedInfoEvent> eventCaptor =
                ArgumentCaptor.forClass(CustomerUpdatedInfoEvent.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        CustomerUpdatedInfoEvent published = eventCaptor.getValue();
        assertEquals(customerId, published.customerId());
        assertEquals("Surname", published.changed());

        verifyNoMoreInteractions(domainLookupService, validator, customerWriter, publisher);
    }

    @Test
    void handleUpdateSurname_NoChange() {
        UUID customerId = UUID.randomUUID();
        String password = "secret";
        String lastName = " Cena ";
        String trimmed = "Cena";
        Customer customer = mock(Customer.class);

        when(customer.getCustomerId()).thenReturn(customerId);
        when(domainLookupService.getCustomerOrThrow(eq(customerId), any()))
                .thenReturn(customer);
        when(validator.hasNoUpdate(any(Customer.class), eq(trimmed), any()))
                .thenReturn(true); // <-- NO CHANGE

        customerServiceActions.handleUpdateSurname(customerId, password, lastName);

        verify(validator).verifyCustomer(eq(customerId), anyString());
        verify(validator).requireNotBlank(eq(lastName), eq(customerId), anyString(),
                anyString(), anyString());
        verify(domainLookupService).getCustomerOrThrow(eq(customerId), any());
        verify(validator).verifyPasswordOrThrow(any(Customer.class), eq(password), eq(customerId), anyString());
        verify(validator).hasNoUpdate(any(Customer.class), eq(trimmed), anyString());

        verify(customer, never()).setSurname(anyString());
        verify(customerWriter, never()).save(any());
        verify(publisher, never()).publishEvent(any());

        verifyNoMoreInteractions(domainLookupService, validator, customerWriter, publisher);
    }

    @Test
    void handleUpdateFullName() {
        UUID customerId = UUID.randomUUID();
        String password = "secret";
        String rawName = " John ";
        String rawSurname = " Cena ";
        String newName = "John";
        String newSurname = "Cena";
        Customer customer = mock(Customer.class);

        when(customer.getCustomerId()).thenReturn(customerId);
        when(domainLookupService.getCustomerOrThrow(eq(customerId), anyString()))
                .thenReturn(customer);
        when(validator.hasNoUpdate(eq(customer), eq(newName), anyString()))
                .thenReturn(false);
        when(validator.hasNoUpdate(eq(customer), eq(newSurname), anyString()))
                .thenReturn(false);

        customerServiceActions.handleUpdateFullName(customerId, password, rawName, rawSurname);

        verify(validator).verifyCustomer(eq(customerId), anyString());
        verify(validator).requireNotBlank(eq(rawName), eq(customerId), anyString(),
                anyString(), anyString());
        verify(validator).requireNotBlank(eq(rawSurname), eq(customerId), anyString(),
                anyString(), anyString());
        verify(domainLookupService).getCustomerOrThrow(eq(customerId), anyString());
        verify(validator).verifyPasswordOrThrow(eq(customer), eq(password), eq(customerId), anyString());
        verify(validator).hasNoUpdate(eq(customer), eq(newName), anyString());
        verify(validator).hasNoUpdate(eq(customer), eq(newSurname), anyString());

        verify(validator, never()).auditNoChange(any());


        verify(customer).setName(newName);
        verify(customer).setSurname(newSurname);
        verify(customerWriter).save(customer);


        ArgumentCaptor<CustomerUpdatedInfoEvent> eventCaptor =
                ArgumentCaptor.forClass(CustomerUpdatedInfoEvent.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        CustomerUpdatedInfoEvent published = eventCaptor.getValue();
        assertEquals(customerId, published.customerId());
        assertEquals("Name & Surname", published.changed());

        verifyNoMoreInteractions(domainLookupService, validator, customerWriter, publisher);
    }

    @Test
    void handleUpdateFullName_OnlyNameChanged() {
        UUID customerId = UUID.randomUUID();
        String password = "secret";
        String rawName = " John ";
        String rawSurname = " Cena "; // same as existing
        String newName = "John";
        String newSurname = "Cena";

        Customer customer = mock(Customer.class);
        when(customer.getCustomerId()).thenReturn(customerId);

        when(domainLookupService.getCustomerOrThrow(eq(customerId), anyString()))
                .thenReturn(customer);

        when(validator.hasNoUpdate(eq(customer), eq(newName), anyString()))
                .thenReturn(false);

        when(validator.hasNoUpdate(eq(customer), eq(newSurname), anyString()))
                .thenReturn(true);

        customerServiceActions.handleUpdateFullName(customerId, password, rawName, rawSurname);

        verify(validator).verifyCustomer(eq(customerId), anyString());
        verify(validator).requireNotBlank(eq(rawName), eq(customerId), anyString(), anyString(), anyString());
        verify(validator).requireNotBlank(eq(rawSurname), eq(customerId), anyString(), anyString(), anyString());
        verify(domainLookupService).getCustomerOrThrow(eq(customerId), anyString());
        verify(validator).verifyPasswordOrThrow(eq(customer), eq(password), eq(customerId), anyString());
        verify(validator).hasNoUpdate(eq(customer), eq(newName), anyString());
        verify(validator).hasNoUpdate(eq(customer), eq(newSurname), anyString());

        verify(customer).setName(newName);
        verify(customer, never()).setSurname(anyString());
        verify(customerWriter).save(customer);
        verify(validator, never()).auditNoChange(any());

        ArgumentCaptor<CustomerUpdatedInfoEvent> cap =
                ArgumentCaptor.forClass(CustomerUpdatedInfoEvent.class);
        verify(publisher).publishEvent(cap.capture());
        assertEquals(customerId, cap.getValue().customerId());
        assertEquals("Name & Surname", cap.getValue().changed());

        verifyNoMoreInteractions(domainLookupService, validator, customerWriter, publisher);
    }

    @Test
    void handleUpdateFullName_NoChanges() {
        UUID customerId = UUID.randomUUID();
        String password = "secret";
        String rawName = " John ";
        String rawSurname = " Cena ";
        String newName = "John";
        String newSurname = "Cena";

        Customer customer = mock(Customer.class);
        when(customer.getCustomerId()).thenReturn(customerId);

        when(domainLookupService.getCustomerOrThrow(eq(customerId), anyString()))
                .thenReturn(customer);

        when(validator.hasNoUpdate(eq(customer), eq(newName), anyString()))
                .thenReturn(true);
        when(validator.hasNoUpdate(eq(customer), eq(newSurname), anyString()))
                .thenReturn(true);

        customerServiceActions.handleUpdateFullName(customerId, password, rawName, rawSurname);

        verify(validator).verifyCustomer(eq(customerId), anyString());
        verify(validator).requireNotBlank(eq(rawName), eq(customerId), anyString(), anyString(), anyString());
        verify(validator).requireNotBlank(eq(rawSurname), eq(customerId), anyString(), anyString(), anyString());
        verify(domainLookupService).getCustomerOrThrow(eq(customerId), anyString());
        verify(validator).verifyPasswordOrThrow(eq(customer), eq(password), eq(customerId), anyString());
        verify(validator).hasNoUpdate(eq(customer), eq(newName), anyString());
        verify(validator).hasNoUpdate(eq(customer), eq(newSurname), anyString());

        verify(customer, never()).setName(anyString());
        verify(customer, never()).setSurname(anyString());

        verify(customerWriter, never()).save(any());

        verify(validator).auditNoChange(customerId);

        verify(publisher, never()).publishEvent(any());

        verifyNoMoreInteractions(domainLookupService, validator, customerWriter, publisher);
    }

    @Test
    void handleUpdateUserName() {
        UUID customerId = UUID.randomUUID();
        String password = "secret";
        String rawUsername = "  johnny  ";
        String trimmed = "johnny";
        Customer customer = mock(Customer.class);

        when(customer.getCustomerId()).thenReturn(customerId);
        when(domainLookupService.getCustomerOrThrow(eq(customerId), any()))
                .thenReturn(customer);
        when(validator.hasNoUpdate(any(Customer.class), eq(trimmed), any()))
                .thenReturn(false);

        customerServiceActions.handleUpdateUserName(customerId, password, rawUsername);

        verify(validator).verifyCustomer(eq(customerId), any());
        verify(validator).requireNotBlank(eq(rawUsername), eq(customerId), any(),
                any(), any());
        verify(domainLookupService).getCustomerOrThrow(eq(customerId), any());
        verify(validator).verifyPasswordOrThrow(any(Customer.class), eq(password), eq(customerId), any());
        verify(validator).hasNoUpdate(any(Customer.class), eq(trimmed), any());

        verify(customer).setUsername(trimmed);
        verify(customerWriter).save(customer);

        ArgumentCaptor<CustomerUpdatedInfoEvent> eventCaptor =
                ArgumentCaptor.forClass(CustomerUpdatedInfoEvent.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        CustomerUpdatedInfoEvent published = eventCaptor.getValue();
        assertEquals(customerId, published.customerId());
        assertEquals("Username", published.changed());

        verifyNoMoreInteractions(domainLookupService, validator, customerWriter, publisher);
    }

    @Test
    void handleUpdateUserName_NoChange() {
        UUID customerId = UUID.randomUUID();
        String password = "secret";
        String rawUsername = "  johnny  ";
        String trimmed = "johnny";
        Customer customer = mock(Customer.class);

        when(customer.getCustomerId()).thenReturn(customerId);
        when(domainLookupService.getCustomerOrThrow(eq(customerId), any()))
                .thenReturn(customer);
        when(validator.hasNoUpdate(any(Customer.class), eq(trimmed), any()))
                .thenReturn(true); // <-- no change

        customerServiceActions.handleUpdateUserName(customerId, password, rawUsername);

        verify(validator).verifyCustomer(eq(customerId), any());
        verify(validator).requireNotBlank(eq(rawUsername), eq(customerId), any(),
                any(), any());
        verify(domainLookupService).getCustomerOrThrow(eq(customerId), any());
        verify(validator).verifyPasswordOrThrow(any(Customer.class), eq(password), eq(customerId), any());
        verify(validator).hasNoUpdate(any(Customer.class), eq(trimmed), any());

        verify(customer, never()).setUsername(anyString());
        verify(customerWriter, never()).save(any());
        verify(publisher, never()).publishEvent(any());

        verifyNoMoreInteractions(domainLookupService, validator, customerWriter, publisher);
    }

    @Test
    void handleUpdateUserPassword_success() {
        UUID customerId = UUID.randomUUID();
        String current = "oldPass";
        String newPass = "newStrongPass!";
        String hashed = "HASHED";
        Customer customer = mock(Customer.class);

        when(customer.getCustomerId()).thenReturn(customerId);
        when(domainLookupService.getCustomerOrThrow(eq(customerId), any()))
                .thenReturn(customer);
        doNothing().when(validator).verifyPasswordIntegrity(eq(newPass), eq(customerId));
        when(validator.verifyPasswordDuplication(eq(newPass), eq(customer)))
                .thenReturn(hashed);

        customerServiceActions.handleUpdateUserPassword(customerId, current, newPass);

        verify(validator).verifyCustomer(eq(customerId), any());
        verify(validator).requireNotBlank(eq(current), eq(customerId), any(), any(), any());
        verify(validator).requireNotBlank(eq(newPass), eq(customerId), any(), any(), any());
        verify(domainLookupService).getCustomerOrThrow(eq(customerId), any());
        verify(validator).verifyPasswordOrThrow(eq(customer), eq(current), eq(customerId), any());

        verify(validator).verifyPasswordIntegrity(eq(newPass), eq(customerId));
        verify(validator).verifyPasswordDuplication(eq(newPass), eq(customer));

        verify(customer).setPasswordHash(eq(hashed));
        verify(customerWriter).save(eq(customer));

        ArgumentCaptor<CustomerSuccessfulOrFailedUpdatePasswordEvent> eventCaptor =
                ArgumentCaptor.forClass(CustomerSuccessfulOrFailedUpdatePasswordEvent.class);

        verify(publisher).publishEvent(eventCaptor.capture());
        assertTrue(eventCaptor.getValue().successfulOrNot());

        verifyNoMoreInteractions(domainLookupService, validator, customerWriter, publisher);
    }

    @Test
    void handleUpdateUserPassword_integrityFailure() {
        UUID customerId = UUID.randomUUID();
        String current = "oldPass";
        String newPass = "weakpass";

        Customer customer = mock(Customer.class);
        when(domainLookupService.getCustomerOrThrow(eq(customerId), any()))
                .thenReturn(customer);

        // integrity throws → failure path
        doThrow(new IllegalArgumentException("weak"))
                .when(validator).verifyPasswordIntegrity(eq(newPass), eq(customerId));

        customerServiceActions.handleUpdateUserPassword(customerId, current, newPass);

        ArgumentCaptor<CustomerSuccessfulOrFailedUpdatePasswordEvent> eventCaptor =
                ArgumentCaptor.forClass(CustomerSuccessfulOrFailedUpdatePasswordEvent.class);

        verify(validator, never()).verifyPasswordDuplication(anyString(), any());
        verify(publisher).publishEvent(eventCaptor.capture());
        assertFalse(eventCaptor.getValue().successfulOrNot());

        verify(validator).verifyCustomer(eq(customerId), any());
        verify(validator).requireNotBlank(eq(current), eq(customerId), any(), any(), any());
        verify(validator).requireNotBlank(eq(newPass), eq(customerId), any(), any(), any());
        verify(domainLookupService).getCustomerOrThrow(eq(customerId), any());
        verify(validator).verifyPasswordOrThrow(eq(customer), eq(current), eq(customerId), any());
        verify(validator, times(1)).verifyPasswordIntegrity(eq(newPass), eq(customerId));
        verify(validator, never()).verifyPasswordDuplication(any(), any());


        verify(customer, never()).setPasswordHash(any());
        verify(customerWriter, never()).save(any());

        verifyNoMoreInteractions(domainLookupService, validator, customerWriter, publisher);
    }

    @Test
    void handleUpdateUserPassword_duplicationFailure() {
        UUID customerId = UUID.randomUUID();
        String current = "oldPass";
        String newPass = "sameAsOld";

        Customer customer = mock(Customer.class);
        when(customer.getCustomerId()).thenReturn(customerId);
        when(domainLookupService.getCustomerOrThrow(eq(customerId), any()))
                .thenReturn(customer);

        doNothing().when(validator).verifyPasswordIntegrity(eq(newPass), eq(customerId));

        doThrow(new IllegalArgumentException("duplicate"))
                .when(validator).verifyPasswordDuplication(eq(newPass), eq(customer));

        customerServiceActions.handleUpdateUserPassword(customerId, current, newPass);


        ArgumentCaptor<CustomerSuccessfulOrFailedUpdatePasswordEvent> eventCaptor =
                ArgumentCaptor.forClass(CustomerSuccessfulOrFailedUpdatePasswordEvent.class);

        verify(publisher).publishEvent(eventCaptor.capture());
        assertFalse(eventCaptor.getValue().successfulOrNot());

        verify(validator).verifyCustomer(eq(customerId), any());
        verify(validator).requireNotBlank(eq(current), eq(customerId), any(), any(), any());
        verify(validator).requireNotBlank(eq(newPass), eq(customerId), any(), any(), any());
        verify(domainLookupService).getCustomerOrThrow(eq(customerId), any());
        verify(validator).verifyPasswordOrThrow(eq(customer), eq(current), eq(customerId), any());
        verify(validator).verifyPasswordIntegrity(eq(newPass), eq(customerId));

        verify(customer, never()).setPasswordHash(any());
        verify(customerWriter, never()).save(any());
        verify(validator).verifyPasswordDuplication(eq(newPass), eq(customer));

        verifyNoMoreInteractions(domainLookupService, validator, customerWriter, publisher);
    }
}