package commerce.eshop.core.tests.application.orderTest.writer;

import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.order.writer.OrderWriter;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.repository.OrderItemRepo;
import commerce.eshop.core.repository.OrderRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

class OrderWriterTest {

    // == Fields ==
    private OrderRepo orderRepo;
    private OrderItemRepo orderItemRepo;
    private CentralAudit centralAudit;
    private OrderWriter orderWriter;

    // == Tests ==

    @BeforeEach
    void setUp() {
        orderRepo = mock(OrderRepo.class);
        orderItemRepo = mock(OrderItemRepo.class);
        centralAudit = mock(CentralAudit.class);

        orderWriter = new OrderWriter(orderRepo, orderItemRepo, centralAudit);
        mockAuditReturnSame(centralAudit);
    }

    @Test
    void save_success() {
        UUID cartId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Order order = mock(Order.class);
        Order saved = mock(Order.class);
        UUID orderId = UUID.randomUUID();

        when(orderRepo.saveAndFlush(order)).thenReturn(saved);
        when(saved.getOrderId()).thenReturn(orderId);

        Order result = orderWriter.save(order, cartId, customerId);

        assertSame(saved, result);

        verify(orderRepo, times(1)).saveAndFlush(order);
        verify(orderItemRepo, times(1)).snapShotFromCart(orderId, cartId);
        verify(orderItemRepo, times(1)).clearCart(cartId);

        verifyNoMoreInteractions(centralAudit);
    }

    @Test
    void save_withCart_success_snapshotsAndClearsCart() {
        UUID cartId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Order order = mock(Order.class);
        Order saved = mock(Order.class);
        UUID orderId = UUID.randomUUID();

        when(orderRepo.saveAndFlush(order)).thenReturn(saved);
        when(saved.getOrderId()).thenReturn(orderId);

        Order result = orderWriter.save(order, cartId, customerId);

        assertSame(saved, result);

        verify(orderRepo, times(1)).saveAndFlush(order);
        verify(orderItemRepo, times(1)).snapShotFromCart(orderId, cartId);
        verify(orderItemRepo, times(1)).clearCart(cartId);

        verifyNoMoreInteractions(centralAudit);
    }

    @Test
    void save__dataIntegrityViolation_throw() {
        UUID cartId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Order order = mock(Order.class);
        DataIntegrityViolationException dup =
                new DataIntegrityViolationException("dup");

        when(orderRepo.saveAndFlush(order)).thenThrow(dup);

        DataIntegrityViolationException ex = assertThrows(
                DataIntegrityViolationException.class,
                () -> orderWriter.save(order, cartId, customerId)
        );

        assertSame(dup, ex);

        verify(orderRepo, times(1)).saveAndFlush(order);
        verify(orderItemRepo, never()).snapShotFromCart(any(UUID.class), any(UUID.class));
        verify(orderItemRepo, never()).clearCart(any(UUID.class));

        verify(centralAudit, times(1)).audit(
                eq(dup),
                eq(customerId),
                eq(EndpointsNameMethods.ORDER_PLACE),
                eq(AuditingStatus.ERROR),
                eq(dup.toString())
        );
    }

    @Test
    void save_generic_success() {
        UUID customerId = UUID.randomUUID();
        Order order = mock(Order.class);
        Order saved = mock(Order.class);

        when(orderRepo.saveAndFlush(order)).thenReturn(saved);

        Order result = orderWriter.save(order, customerId, "GENERIC_ENDPOINT");

        assertSame(saved, result);
        verify(orderRepo, times(1)).saveAndFlush(order);
        verifyNoMoreInteractions(centralAudit);
    }

    @Test
    void save_generic_dataIntegrityViolation_auditedAndRethrown() {
        UUID customerId = UUID.randomUUID();
        Order order = mock(Order.class);
        DataIntegrityViolationException dup =
                new DataIntegrityViolationException("dup");

        when(orderRepo.saveAndFlush(order)).thenThrow(dup);

        DataIntegrityViolationException ex = assertThrows(
                DataIntegrityViolationException.class,
                () -> orderWriter.save(order, customerId, "GENERIC_ENDPOINT")
        );

        assertSame(dup, ex);

        verify(orderRepo, times(1)).saveAndFlush(order);
        verify(centralAudit, times(1)).audit(
                eq(dup),
                eq(customerId),
                eq("GENERIC_ENDPOINT"),
                eq(AuditingStatus.ERROR),
                eq(dup.toString())
        );
    }

    @Test
    void countOrderItems_success() {
        UUID orderId = UUID.randomUUID();

        when(orderRepo.countProductRowsToBeUpdated(orderId)).thenReturn(5);

        int result = orderWriter.countOrderItems(orderId);

        assertEquals(5, result);
        verify(orderRepo, times(1)).countProductRowsToBeUpdated(orderId);
        verifyNoMoreInteractions(centralAudit);
    }

    @Test
    void restoreProductStock_success() {
        UUID orderId = UUID.randomUUID();

        when(orderRepo.restoreProductStockFromOrder(orderId)).thenReturn(3);

        int result = orderWriter.restoreProductStock(orderId);

        assertEquals(3, result);
        verify(orderRepo, times(1)).restoreProductStockFromOrder(orderId);
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