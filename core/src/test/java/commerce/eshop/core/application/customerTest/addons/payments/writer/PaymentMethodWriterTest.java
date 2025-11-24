package commerce.eshop.core.application.customerTest.addons.payments.writer;

import commerce.eshop.core.application.customer.addons.payments.writer.PaymentMethodWriter;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.CustomerPaymentMethod;
import commerce.eshop.core.repository.CustomerPaymentMethodRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PaymentMethodWriterTest {

    private CustomerPaymentMethodRepo customerPaymentMethodRepo;
    private CentralAudit centralAudit;
    private PaymentMethodWriter paymentMethodWriter;

    @BeforeEach
    void setUp() {
        customerPaymentMethodRepo = mock(CustomerPaymentMethodRepo.class);
        centralAudit = mock(CentralAudit.class);

        paymentMethodWriter = new PaymentMethodWriter(customerPaymentMethodRepo, centralAudit);
        mockAuditReturnSame(centralAudit);
    }

    @Test
    void updateDefaultToFalse_callsRepo() {
        UUID customerId = UUID.randomUUID();

        when(customerPaymentMethodRepo.updateDefaultMethodToFalse(customerId)).thenReturn(1);

        paymentMethodWriter.updateDefaultToFalse(customerId);

        verify(customerPaymentMethodRepo).updateDefaultMethodToFalse(customerId);
    }

    @Test
    void updateDefaultToFalse_logsWhenZero_noAudit() {
        UUID customerId = UUID.randomUUID();

        when(customerPaymentMethodRepo.updateDefaultMethodToFalse(customerId)).thenReturn(0);

        paymentMethodWriter.updateDefaultToFalse(customerId);

        verify(customerPaymentMethodRepo).updateDefaultMethodToFalse(customerId);
        verifyNoInteractions(centralAudit);
    }

    @Test
    void save_success() {
        CustomerPaymentMethod paymentMethod = mock(CustomerPaymentMethod.class);
        CustomerPaymentMethod savedPaymentMethod = mock(CustomerPaymentMethod.class);
        UUID customerId = UUID.randomUUID();
        Customer customer = mock(Customer.class);

        when(paymentMethod.getCustomer()).thenReturn(customer);
        when(customer.getCustomerId()).thenReturn(customerId);
        when(customerPaymentMethodRepo.saveAndFlush(paymentMethod)).thenReturn(savedPaymentMethod);

        CustomerPaymentMethod result = paymentMethodWriter.save(paymentMethod, "EP");


        assertSame(savedPaymentMethod, result);
        verify(customerPaymentMethodRepo).saveAndFlush(paymentMethod);
        verifyNoInteractions(centralAudit);
    }

    @Test
    void  save_duplicate_throwsAudited() {
        UUID customerId = UUID.randomUUID();
        Customer customer = mock(Customer.class);
        CustomerPaymentMethod paymentMethod = mock(CustomerPaymentMethod.class);
        DataIntegrityViolationException dive = new DataIntegrityViolationException("dup!");

        when(paymentMethod.getCustomer()).thenReturn(customer);
        when(customer.getCustomerId()).thenReturn(customerId);
        when(customerPaymentMethodRepo.saveAndFlush(paymentMethod)).thenThrow(dive);

        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
                () -> paymentMethodWriter.save(paymentMethod, "EP"));

        assertSame(dive, exception);

        verify(centralAudit).audit(
                eq(dive),
                eq(customerId),
                eq("EP"),
                eq(AuditingStatus.ERROR),
                anyString()
        );
    }

    @Test
    void delete_success() {
        UUID customerId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        when(customerPaymentMethodRepo.deleteByCustomer_CustomerIdAndCustomerPaymentId(customerId, paymentId)).thenReturn(1L);

        long result = paymentMethodWriter.delete(customerId, paymentId);

        assertEquals(1L, result);
        verify(customerPaymentMethodRepo).deleteByCustomer_CustomerIdAndCustomerPaymentId(customerId, paymentId);
        verifyNoInteractions(centralAudit);
    }

    @Test
    void delete_notFound_throwsAudited() {
        UUID customerId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        when(customerPaymentMethodRepo.deleteByCustomer_CustomerIdAndCustomerPaymentId(customerId, paymentId)).thenReturn(0L);

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> paymentMethodWriter.delete(customerId, paymentId));

        assertEquals("Payment method not found: " + paymentId, ex.getMessage());

        verify(centralAudit).audit(
                any(NoSuchElementException.class),
                eq(customerId),
                eq(EndpointsNameMethods.PM_DELETE),
                eq(AuditingStatus.WARNING),
                contains("Payment method not found")
        );
    }

    @Test
    void delete_duplicate_throwsAudited() {
        UUID customerId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        DataIntegrityViolationException dive = new DataIntegrityViolationException("foreign key");

        when(customerPaymentMethodRepo.deleteByCustomer_CustomerIdAndCustomerPaymentId(customerId, paymentId)).thenThrow(dive);

        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
                () -> paymentMethodWriter.delete(customerId, paymentId));

        assertSame(dive, exception);

        verify(centralAudit).audit(
                eq(dive),
                eq(customerId),
                eq(EndpointsNameMethods.PM_DELETE),
                eq(AuditingStatus.ERROR),
                anyString()
        );
    }

        // == Private Methods ==
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