package commerse.eshop.core.application.customerTest.validator;

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
    void verifyPasswordDuplication() {
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
    }

    @Test
    void verifyCustomer() {
    }

    @Test
    void isNoChange() {
    }

    @Test
    void auditNoChange() {
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
                eq(AuditingStatus.WARNING),
                eq(code)
        );
    }

    private <E extends RuntimeException> E auditAndThrow(E ex, UUID cid, String method, String code) {
        return centralAudit.audit(ex, cid, method, AuditingStatus.WARNING, code);
    }

    static <E extends RuntimeException> void mockAuditReturnSame(CentralAudit auditMock) {
        when(auditMock.audit(
                any(RuntimeException.class),
                any(UUID.class),
                anyString(),
                any(AuditingStatus.class),
                anyString()
        )).thenAnswer(inv -> inv.getArgument(0));
    }
}