package commerce.eshop.core.tests.application.productTest.writer;

import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.order.writer.OrderCartWriter;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.repository.CartItemRepo;
import commerce.eshop.core.repository.DbLockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

class OrderCartWriterTest {

    // == Fields ==
    private DbLockRepository dbLockRepo;
    private CartItemRepo cartItemRepo;
    private CentralAudit centralAudit;
    private OrderCartWriter orderCartWriter;

    // == Tests ==

    @BeforeEach
    void setUp() {
        dbLockRepo = mock(DbLockRepository.class);
        cartItemRepo = mock(CartItemRepo.class);
        centralAudit = mock(CentralAudit.class);

        orderCartWriter = new OrderCartWriter(dbLockRepo, cartItemRepo, centralAudit);
        mockAuditReturnSame(centralAudit);
    }

    @Test
    void reserveStock_success_noAudit() {
        UUID cartId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        // updated == expected → no error
        when(cartItemRepo.reserveStockForCart(cartId)).thenReturn(3);
        when(cartItemRepo.countDistinctCartProducts(cartId)).thenReturn(3L);

        assertDoesNotThrow(() -> orderCartWriter.reserveStock(cartId, customerId));

        verify(cartItemRepo, times(1)).reserveStockForCart(cartId);
        verify(cartItemRepo, times(1)).countDistinctCartProducts(cartId);
        verifyNoMoreInteractions(centralAudit);
    }

    @Test
    void reserveStock_mismatch_throw() {
        UUID cartId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        // updated != expected → insufficient stock
        when(cartItemRepo.reserveStockForCart(cartId)).thenReturn(1);
        when(cartItemRepo.countDistinctCartProducts(cartId)).thenReturn(2L);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> orderCartWriter.reserveStock(cartId, customerId)
        );

        assertEquals("409 CONFLICT \"INSUFFICIENT_STOCK\"", ex.getMessage());
        assertEquals("INSUFFICIENT_STOCK", ex.getReason());

        verify(cartItemRepo, times(1)).reserveStockForCart(cartId);
        verify(cartItemRepo, times(1)).countDistinctCartProducts(cartId);
        verify(centralAudit, times(1)).audit(
                any(ResponseStatusException.class),
                eq(customerId),
                eq(EndpointsNameMethods.ORDER_PLACE),
                eq(AuditingStatus.WARNING),
                eq("INSUFFICIENT_STOCK")
        );
    }

    @Test
    void sumCartOutstanding_success() {
        UUID cartId = UUID.randomUUID();
        BigDecimal total = new BigDecimal("32.50");

        when(cartItemRepo.sumCartTotalOutstanding(cartId)).thenReturn(total);

        BigDecimal result = orderCartWriter.sumCartOutstanding(cartId);

        assertSame(total, result);
        verify(cartItemRepo, times(1)).sumCartTotalOutstanding(cartId);
        verifyNoMoreInteractions(centralAudit);
    }

    @Test
    void lockCart_success() {
        UUID cartId = UUID.randomUUID();

        when(dbLockRepo.tryLockCart(cartId)).thenReturn(true);

        boolean result = orderCartWriter.lockCart(cartId);

        assertTrue(result);
        verify(dbLockRepo, times(1)).tryLockCart(cartId);
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