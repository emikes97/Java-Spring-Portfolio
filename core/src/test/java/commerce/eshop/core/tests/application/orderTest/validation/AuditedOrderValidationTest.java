package commerce.eshop.core.tests.application.orderTest.validation;

import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.order.validation.AuditedOrderValidation;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.application.util.enums.OrderStatus;
import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

class AuditedOrderValidationTest {

    // == Fields ==
    private CentralAudit centralAudit;
    private AuditedOrderValidation auditedOrderValidation;

    @BeforeEach
    void setUp() {
        centralAudit = mock(CentralAudit.class);

        auditedOrderValidation = new AuditedOrderValidation(centralAudit);
        mockAuditReturnSame(centralAudit);
    }

    @Test
    void checkCustomerId_null_throw() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> auditedOrderValidation.checkCustomerId(null)
        );

        assertEquals("The identification key can't be empty", ex.getMessage());

        verify(centralAudit, times(1)).audit(
                any(IllegalArgumentException.class),
                isNull(),
                eq(EndpointsNameMethods.ORDER_PLACE),
                eq(AuditingStatus.WARNING),
                anyString()
        );
    }

    @Test
    void checkCustomerId_nonNull_success() {
        UUID customerId = UUID.randomUUID();

        assertDoesNotThrow(() -> auditedOrderValidation.checkCustomerId(customerId));

        verifyNoMoreInteractions(centralAudit);
    }

    @Test
    void checkAddressDto_null_returnsTrue() {
        assertTrue(auditedOrderValidation.checkAddressDto(null));
        verifyNoInteractions(centralAudit);
    }

    @Test
    void checkAddressDto_nonNull_returnsFalse() {
        DTOOrderCustomerAddress dto = mock(DTOOrderCustomerAddress.class);

        assertFalse(auditedOrderValidation.checkAddressDto(dto));
        verifyNoInteractions(centralAudit);
    }

    @Test
    void checkOutstanding_null_throw() {
        UUID customerId = UUID.randomUUID();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> auditedOrderValidation.checkOutstanding(null, customerId)
        );

        assertEquals("The cart is empty", ex.getMessage());

        verify(centralAudit, times(1)).audit(
                any(IllegalStateException.class),
                eq(customerId),
                eq(EndpointsNameMethods.ORDER_PLACE),
                eq(AuditingStatus.WARNING),
                anyString()
        );
    }

    @Test
    void checkOutstanding_zeroOrNegative_throw() {
        UUID customerId = UUID.randomUUID();

        IllegalStateException exZero = assertThrows(
                IllegalStateException.class,
                () -> auditedOrderValidation.checkOutstanding(BigDecimal.ZERO, customerId)
        );
        assertEquals("The cart is empty", exZero.getMessage());

        // negative
        IllegalStateException exNegative = assertThrows(
                IllegalStateException.class,
                () -> auditedOrderValidation.checkOutstanding(new BigDecimal("-1.00"), customerId)
        );
        assertEquals("The cart is empty", exNegative.getMessage());

        verify(centralAudit, atLeastOnce()).audit(
                any(IllegalStateException.class),
                eq(customerId),
                eq(EndpointsNameMethods.ORDER_PLACE),
                eq(AuditingStatus.WARNING),
                anyString()
        );
    }

    @Test
    void checkOutstanding_positive_success() {
        UUID customerId = UUID.randomUUID();
        BigDecimal total = new BigDecimal("15.25");

        assertDoesNotThrow(() -> auditedOrderValidation.checkOutstanding(total, customerId));

        verifyNoMoreInteractions(centralAudit);
    }

    @Test
    void checkCustomerAndOrder_missingIds_throw() {
        UUID customerId = null;
        UUID orderId = UUID.randomUUID();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> auditedOrderValidation.checkCustomerAndOrder(customerId, orderId)
        );

        assertEquals("Missing customerId/orderId.", ex.getMessage());

        verify(centralAudit, times(1)).audit(
                any(IllegalArgumentException.class),
                isNull(), // customerId is null in this call
                eq(EndpointsNameMethods.ORDER_CANCEL),
                eq(AuditingStatus.WARNING),
                eq("MISSING_IDS")
        );
    }

    @Test
    void checkCustomerAndOrder_allPresent_success() {
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        assertDoesNotThrow(
                () -> auditedOrderValidation.checkCustomerAndOrder(customerId, orderId)
        );

        verifyNoMoreInteractions(centralAudit);
    }

    @Test
    void checkOrderStatus_notPendingPayment_throw() {
        UUID customerId = UUID.randomUUID();
        OrderStatus status = OrderStatus.PAID; // anything != PENDING_PAYMENT

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> auditedOrderValidation.checkOrderStatus(status, customerId)
        );

        assertEquals("INVALID_STATE" + status, ex.getMessage());

        verify(centralAudit, times(1)).audit(
                any(IllegalStateException.class),
                eq(customerId),
                eq(EndpointsNameMethods.ORDER_CANCEL),
                eq(AuditingStatus.WARNING),
                anyString()
        );
    }

    @Test
    void checkOrderStatus_pendingPayment_success() {
        UUID customerId = UUID.randomUUID();
        OrderStatus status = OrderStatus.PENDING_PAYMENT;

        assertDoesNotThrow(
                () -> auditedOrderValidation.checkOrderStatus(status, customerId)
        );

        verifyNoMoreInteractions(centralAudit);
    }

    @Test
    void checkExpectedUpdated_zero_throw() {
        UUID customerId = UUID.randomUUID();

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> auditedOrderValidation.checkExpectedUpdated(0, customerId)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("ORDER_ITEMS_EMPTY_OR_MISSING", ex.getReason());

        verify(centralAudit, times(1)).audit(
                any(ResponseStatusException.class),
                eq(customerId),
                eq(EndpointsNameMethods.ORDER_CANCEL),
                eq(AuditingStatus.WARNING),
                eq("ORDER_ITEMS_EMPTY_OR_MISSING")
        );
    }

    @Test
    void checkExpectedUpdated_nonZero_success() {
        UUID customerId = UUID.randomUUID();

        assertDoesNotThrow(
                () -> auditedOrderValidation.checkExpectedUpdated(3, customerId)
        );

        verifyNoMoreInteractions(centralAudit);
    }

    @Test
    void checkRestockUpdate_mismatch_throw() {
        UUID customerId = UUID.randomUUID();
        int updated = 1;
        int expected = 2;

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> auditedOrderValidation.checkRestockUpdate(updated, expected, customerId)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("ORDER_ITEMS_EMPTY_OR_MISSING", ex.getReason());

        verify(centralAudit, times(1)).audit(
                any(ResponseStatusException.class),
                eq(customerId),
                eq(EndpointsNameMethods.ORDER_CANCEL),
                eq(AuditingStatus.WARNING),
                eq("ORDER_ITEMS_EMPTY_OR_MISSING")
        );
    }

    @Test
    void checkRestockUpdate_match_success() {
        UUID customerId = UUID.randomUUID();
        int updated = 5;
        int expected = 5;

        assertDoesNotThrow(
                () -> auditedOrderValidation.checkRestockUpdate(updated, expected, customerId)
        );

        verifyNoMoreInteractions(centralAudit);
    }

    // == Private Methods ==
    static <E extends RuntimeException> void mockAuditReturnSame(CentralAudit auditMock) {
        // 5-arg overload
        when(auditMock.audit(
                any(RuntimeException.class),
                nullable(UUID.class),
                anyString(),
                any(AuditingStatus.class),
                anyString()
        )).thenAnswer(inv -> inv.getArgument(0));

        // 4-arg overload
        when(auditMock.audit(
                any(RuntimeException.class),
                nullable(UUID.class),
                anyString(),
                any(AuditingStatus.class)
        )).thenAnswer(inv -> inv.getArgument(0));
    }
}