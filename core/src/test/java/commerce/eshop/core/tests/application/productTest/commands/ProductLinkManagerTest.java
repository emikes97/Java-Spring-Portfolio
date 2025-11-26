package commerce.eshop.core.tests.application.productTest.commands;

import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.product.commands.ProductLinkManager;
import commerce.eshop.core.application.product.writer.ProductWriter;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductLinkManagerTest {

    // == Fields ==
    private ProductWriter productWriter;
    private CentralAudit centralAudit;
    private ProductLinkManager productLinkManager;

    // == Tests ==
    @BeforeEach
    void setUp() {
        productWriter = mock(ProductWriter.class);
        centralAudit = mock(CentralAudit.class);

        productLinkManager = new ProductLinkManager(productWriter, centralAudit);
        mockAuditReturnSame(centralAudit);
    }

    @Test
    void link_success() {
        long productId = 1L;
        long categoryId = 2L;

        when(productWriter.link(productId, categoryId)).thenReturn(1);

        int result = productLinkManager.link(productId, categoryId);

        assertEquals(1, result);
        verify(productWriter, times(1)).link(productId, categoryId);
        verifyNoMoreInteractions(centralAudit);
    }

    @Test
    void link_dataIntegrityViolation_throw() {
        long productId = 1L;
        long categoryId = 2L;
        DataIntegrityViolationException dup = new DataIntegrityViolationException("dup");

        when(productWriter.link(productId, categoryId)).thenThrow(dup);

        DataIntegrityViolationException ex = assertThrows(
                DataIntegrityViolationException.class,
                () -> productLinkManager.link(productId, categoryId)
        );

        assertEquals(dup, ex);

        verify(productWriter, times(1)).link(productId, categoryId);
        verify(centralAudit, times(1)).audit(
                eq(dup),
                isNull(),
                eq(EndpointsNameMethods.PRODUCT_LINK),
                eq(AuditingStatus.ERROR),
                eq(dup.toString())
        );
    }

    @Test
    void unlink_success() {
        long productId = 3L;
        long categoryId = 4L;

        when(productWriter.unlink(productId, categoryId)).thenReturn(1);

        int result = productLinkManager.unlink(productId, categoryId);

        assertEquals(1, result);
        verify(productWriter, times(1)).unlink(productId, categoryId);
        verifyNoMoreInteractions(centralAudit);
    }

    @Test
    void unlink_dataIntegrityViolation_throw() {
        long productId = 3L;
        long categoryId = 4L;
        DataIntegrityViolationException dup = new DataIntegrityViolationException("dup");

        when(productWriter.unlink(productId, categoryId)).thenThrow(dup);

        DataIntegrityViolationException ex = assertThrows(
                DataIntegrityViolationException.class,
                () -> productLinkManager.unlink(productId, categoryId)
        );

        assertEquals(dup, ex);

        verify(productWriter, times(1)).unlink(productId, categoryId);
        verify(centralAudit, times(1)).audit(
                eq(dup),
                isNull(),
                eq(EndpointsNameMethods.PRODUCT_UNLINK),
                eq(AuditingStatus.ERROR),
                eq(dup.toString())
        );
    }

    @Test
    void unlink_noSuchElement_throw() {
        long productId = 5L;
        long categoryId = 6L;
        NoSuchElementException notFound = new NoSuchElementException("missing");

        when(productWriter.unlink(productId, categoryId)).thenThrow(notFound);

        NoSuchElementException ex = assertThrows(
                NoSuchElementException.class,
                () -> productLinkManager.unlink(productId, categoryId)
        );

        assertEquals(notFound, ex);

        verify(productWriter, times(1)).unlink(productId, categoryId);
        verify(centralAudit, times(1)).audit(
                eq(notFound),
                isNull(),
                eq(EndpointsNameMethods.PRODUCT_UNLINK),
                eq(AuditingStatus.ERROR),
                eq(notFound.toString())
        );
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