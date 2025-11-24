package commerce.eshop.core.application.customerTest.validator;

import commerce.eshop.core.application.customer.validation.AuditedCustomerValidation;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.model.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditedCustomerValidationTest {

    // == Fields ==
    private CentralAudit centralAudit;
    private AuditedCustomerValidation validation;
    private PasswordEncoder passwordEncoder;

    // == BeforeEach ==
    @BeforeEach
    void setUp(){
        centralAudit = mock(CentralAudit.class);
        passwordEncoder = mock(PasswordEncoder.class);
        validation = new AuditedCustomerValidation(centralAudit, passwordEncoder);

        // enable “return same exception” behavior for ALL tests
        mockAuditReturnSame(centralAudit);
    }

    // == Tests ==

    // --> Valid Case
    @Test
    void requireNotBlank() {
        UUID customerId = UUID.randomUUID();
        assertDoesNotThrow(
                () -> validation.requireNotBlank("hello", customerId, "EP", "CODE", "msg")
        );
        verifyNoInteractions(centralAudit);
    }

    // --> Invalid Case
    @Test
    void requireNotBlank_throwsWhenBlank(){
        UUID customerId = UUID.randomUUID();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validation.requireNotBlank("", customerId, "EP", "CODE", "MISSING"));

        assertEquals("MISSING", ex.getMessage());

        verifyAuditCalledOnce(IllegalArgumentException.class, customerId, "EP", "CODE");
    }

    @Test
    void requireNotBlank_throwsWhenNull(){
        UUID customerId = UUID.randomUUID();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validation.requireNotBlank(null, customerId, "EP", "CODE", "MISSING"));

        assertEquals("MISSING", ex.getMessage());

        verifyAuditCalledOnce(IllegalArgumentException.class, customerId, "EP", "CODE");
    }

    @Test
    void verifyPasswordOrThrow() {
        UUID customerId = UUID.randomUUID();
        Customer customer = mock(Customer.class);

        when(customer.getPasswordHash()).thenReturn("encoded-password");
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);

        assertDoesNotThrow(() ->
                validation.verifyPasswordOrThrow(customer, "password", customerId, "EP")
        );

        verifyNoInteractions(centralAudit);
    }

    @Test
    void verifyPasswordOrThrow_WrongPassword(){
        UUID customerId = UUID.randomUUID();
        Customer customer = mock(Customer.class);

        when(customer.getPasswordHash()).thenReturn("encoded-password");
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> validation.verifyPasswordOrThrow(customer, "wrong", customerId, "EP"));

        assertEquals("Invalid password", ex.getMessage());

        verifyAuditCalledOnce(BadCredentialsException.class, customerId, "EP", "INVALID_PASSWORD");
    }

    @Test
    void verifyPasswordOrThrow_NullRawPassword(){
        UUID customerId = UUID.randomUUID();
        Customer customer = mock(Customer.class);

        when(customer.getPasswordHash()).thenReturn("encoded_password");
        when(passwordEncoder.matches(null, "encoded-password")).thenReturn(false);

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> validation.verifyPasswordOrThrow(customer, null, customerId, "EP"));

        assertEquals("Invalid password", ex.getMessage());

        verifyAuditCalledOnce(BadCredentialsException.class, customerId, "EP", "INVALID_PASSWORD");
    }

    @Test
    void  verifyPasswordDuplication() {
        Customer customer = mock(Customer.class);

        when(customer.getPasswordHash()).thenReturn("encoded_password");
        when(customer.getCustomerId()).thenReturn(UUID.randomUUID());
        when(passwordEncoder.matches("different_encoded_password", "encoded_password")).thenReturn(false);

        assertDoesNotThrow(
                () -> validation.verifyPasswordDuplication("different_encoded_password", customer)
        );

        verifyNoInteractions(centralAudit);
    }

    @Test
    void verifyPasswordDuplication_throwDuplicatePassword(){
        Customer customer = mock(Customer.class);
        UUID customerId = UUID.randomUUID();

        when(customer.getCustomerId()).thenReturn(customerId);
        when(customer.getPasswordHash()).thenReturn("encoded_password");
        when(passwordEncoder.matches("new_password", "encoded_password")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validation.verifyPasswordDuplication("new_password", customer)
        );

        assertEquals("New password must be different from current.", ex.getMessage());
        verifyAuditCalledOnce(IllegalArgumentException.class, customerId, EndpointsNameMethods.UPDATE_PASSWORD, "REUSED_PASSWORD");
    }

    @Test
    void verifyPasswordIntegrity() {
        UUID customerId = UUID.randomUUID();
        String newPassword = "1@%#@F$%!@#$%%&F@#EEDE";

        assertDoesNotThrow(
                () -> validation.verifyPasswordIntegrity(newPassword, customerId)
        );

        verifyNoInteractions(centralAudit);
    }

    @Test
    void verifyPasswordIntegrity_WeakPassword(){
        UUID customerId = UUID.randomUUID();
        String newPassword = "12345";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validation.verifyPasswordIntegrity(newPassword, customerId));

        assertEquals("Password too short.", ex.getMessage());
        verifyAuditCalledOnce(IllegalArgumentException.class, customerId, EndpointsNameMethods.UPDATE_PASSWORD, "WEAK_PASSWORD");
    }

    @Test
    void verifyCustomer() {
        Customer customer = mock(Customer.class);

        when(customer.getCustomerId()).thenReturn(UUID.randomUUID());

        assertDoesNotThrow(
                () -> validation.verifyCustomer(customer.getCustomerId(), "endpoint")
        );

        verifyNoInteractions(centralAudit);
    }

    @Test
    void verifyCustomer_NullException(){
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validation.verifyCustomer(null, "endpoint" ));

        assertEquals("Missing customerId.", ex.getMessage());
        verifyAuditCalledOnce(IllegalArgumentException.class, null, "endpoint", "MISSING_CUSTOMER_ID");
    }

    @Test
    void hasNoUpdate_namePath_SameName() {
        Customer customer = mock(Customer.class);
        UUID customerId = UUID.randomUUID();

        when(customer.getCustomerId()).thenReturn(customerId);
        when(customer.getName()).thenReturn("John");

        String trimmed = "John";

        //
        boolean result = validation.hasNoUpdate(customer, trimmed, EndpointsNameMethods.UPDATE_NAME);

        assertTrue(result);

        verify(centralAudit, times(1)).info(
                eq(customerId),
                eq(EndpointsNameMethods.UPDATE_NAME),
                eq(AuditingStatus.WARNING),
                eq("NO_CHANGE_SAME_NAME")
        );
    }


    @Test
    void hasNoUpdate_namePath_differentName() {
        Customer customer = mock(Customer.class);
        UUID customerId = UUID.randomUUID();

        when(customer.getCustomerId()).thenReturn(customerId);
        when(customer.getName()).thenReturn("John");

        String trimmed = "Nick";

        //
        boolean result = validation.hasNoUpdate(customer, trimmed, EndpointsNameMethods.UPDATE_NAME);

        assertFalse(result);
        verifyNoInteractions(centralAudit);
    }

    @Test
    void hasNoUpdate_surnamePath_sameSurname(){
        Customer customer = mock(Customer.class);
        UUID customerId = UUID.randomUUID();

        when(customer.getSurname()).thenReturn("JoJo");
        when(customer.getCustomerId()).thenReturn(customerId);

        String trimmed = "JoJo";

        boolean result = validation.hasNoUpdate(customer, trimmed, EndpointsNameMethods.UPDATE_SURNAME);

        assertTrue(result);
        verify(centralAudit, times(1)).info(
                eq(customerId),
                eq(EndpointsNameMethods.UPDATE_SURNAME),
                eq(AuditingStatus.WARNING),
                eq("NO_CHANGE_SAME_SURNAME")
        );
    }

    @Test
    void hasNoUpdate_surnamePath_differentSurname(){
        Customer customer = mock(Customer.class);
        UUID customerId = UUID.randomUUID();

        when(customer.getSurname()).thenReturn("JoJo");
        when(customer.getCustomerId()).thenReturn(customerId);

        String trimmed = "Caesar";

        boolean result = validation.hasNoUpdate(customer, trimmed, EndpointsNameMethods.UPDATE_SURNAME);

        assertFalse(result);
        verifyNoInteractions(centralAudit);
    }

    @Test
    void hasNoUpdate_usernamePath_sameUsername(){
        Customer customer = mock(Customer.class);
        UUID customerId = UUID.randomUUID();

        when(customer.getUsername()).thenReturn("JoJo");
        when(customer.getCustomerId()).thenReturn(customerId);

        String trimmed = "JoJo";

        boolean result = validation.hasNoUpdate(customer, trimmed, EndpointsNameMethods.UPDATE_USERNAME);
        assertTrue(result);
        verify(centralAudit, times(1)).info(
                eq(customerId),
                eq(EndpointsNameMethods.UPDATE_USERNAME),
                eq(AuditingStatus.WARNING),
                eq("NO_CHANGE_SAME_USERNAME")
        );
    }

    @Test
    void hasNoUpdate_usernamePath_differentUsername(){
        Customer customer = mock(Customer.class);
        UUID customerId = UUID.randomUUID();

        when(customer.getUsername()).thenReturn("JoJo");
        when(customer.getCustomerId()).thenReturn(customerId);

        String trimmed = "Caesar";

        boolean result = validation.hasNoUpdate(customer, trimmed, EndpointsNameMethods.UPDATE_USERNAME);
        assertFalse(result);
        verifyNoInteractions(centralAudit);
    }

    @Test
    void hasNoUpdate_wrongPath(){
        Customer customer = mock(Customer.class);
        UUID customerId = UUID.randomUUID();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validation.hasNoUpdate(customer, "Caesaaaaar", "RANDOM"));

        verifyAuditCalledOnce(IllegalArgumentException.class, null, "UNREGISTERED_METHOD", "Method passed at hasNoUpdate, doesn't exist");
    }

    @Test
    void auditNoChange() {
        UUID customerId = UUID.randomUUID();

        validation.auditNoChange(customerId);

        verify(centralAudit, times(1)).info(
                eq(customerId),
                eq(EndpointsNameMethods.UPDATE_FULLNAME),
                eq(AuditingStatus.WARNING),
                eq("NO_CHANGE_SAME_FULLNAME")
        );
    }


    // == Helpers for every validation test kit ==

    private void verifyAuditCalledOnce(
            Class<? extends RuntimeException> exType,
            UUID customerId,
            String method,
            String code
    ) {
        verify(centralAudit, times(1)).audit(
                any(exType),
                eq(customerId),
                eq(method),
                any(AuditingStatus.class),
                eq(code)
        );
    }

    static <E extends RuntimeException> void mockAuditReturnSame(CentralAudit auditMock) {
        when(auditMock.audit(
                any(RuntimeException.class),
                any(),
                anyString(),
                any(AuditingStatus.class),
                anyString()
        )).thenAnswer(inv -> inv.getArgument(0));
    }
}