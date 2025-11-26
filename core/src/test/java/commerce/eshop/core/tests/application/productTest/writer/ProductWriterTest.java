package commerce.eshop.core.tests.application.productTest.writer;

import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.product.writer.ProductWriter;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.repository.ProductCategoryRepo;
import commerce.eshop.core.repository.ProductRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductWriterTest {

    private CentralAudit centralAudit;
    private ProductRepo productRepo;
    private ProductCategoryRepo productCategoryRepo;
    private ProductWriter productWriter;

    @BeforeEach
    void setUp() {
        centralAudit = mock(CentralAudit.class);
        productRepo = mock(ProductRepo.class);
        productCategoryRepo = mock(ProductCategoryRepo.class);

        productWriter = new ProductWriter(centralAudit, productRepo, productCategoryRepo);
        mockAuditReturnSame(centralAudit);
    }

    @Test
    void save_success() {
        Product product = mock(Product.class);
        Product saved = mock(Product.class);

        when(productRepo.saveAndFlush(product)).thenReturn(saved);

        Product result = productWriter.save(product, "EP");

        assertSame(saved, result);
        verify(productRepo, times(1)).saveAndFlush(product);
        verifyNoMoreInteractions(centralAudit);
    }

    @Test
    void save_dataIntegrityViolation_throw() {
        Product product = mock(Product.class);
        DataIntegrityViolationException ex1 = new DataIntegrityViolationException("dup");

        when(productRepo.saveAndFlush(product)).thenThrow(ex1);

        DataIntegrityViolationException ex = assertThrows(
                DataIntegrityViolationException.class,
                () -> productWriter.save(product, "EP")
        );

        assertSame(ex1, ex);

        verify(centralAudit, times(1)).audit(
                eq(ex1),
                isNull(),
                eq("EP"),
                eq(AuditingStatus.ERROR),
                eq(ex1.toString())
        );
    }

    @Test
    void save_arithmeticException_throw() {
        Product product = mock(Product.class);
        ArithmeticException arithmetic = new ArithmeticException("overflow");

        when(productRepo.saveAndFlush(product)).thenThrow(arithmetic);

        ArithmeticException ex = assertThrows(
                ArithmeticException.class,
                () -> productWriter.save(product, "EP")
        );

        assertSame(arithmetic, ex);

        verify(centralAudit, times(1)).audit(
                eq(arithmetic),
                isNull(),
                eq("EP"),
                eq(AuditingStatus.ERROR),
                eq(arithmetic.toString())
        );
    }

    @Test
    void delete_success() {
        Product product = mock(Product.class);

        productWriter.delete(product);

        verify(productRepo, times(1)).delete(product);
        verify(productRepo, times(1)).flush();
        verifyNoMoreInteractions(centralAudit);
    }

    @Test
    void delete_dataIntegrityViolation_throw() {
        Product product = mock(Product.class);
        DataIntegrityViolationException dup = new DataIntegrityViolationException("fk fail");

        doThrow(dup).when(productRepo).delete(product);

        DataIntegrityViolationException ex = assertThrows(
                DataIntegrityViolationException.class,
                () -> productWriter.delete(product)
        );

        assertSame(dup, ex);

        verify(centralAudit, times(1)).audit(
                eq(dup),
                isNull(),
                eq(EndpointsNameMethods.PRODUCT_REMOVE),
                eq(AuditingStatus.ERROR),
                eq(dup.toString())
        );
        verify(productRepo, never()).flush();
    }

    @Test
    void link_success() {
        long productId = 2L;
        long categoryId = 1L;

        when(productCategoryRepo.linkIfAbsent(productId, categoryId)).thenReturn(1);

        int result = productWriter.link(productId, categoryId);

        assertEquals(1, result);
        verify(productCategoryRepo, times(1)).linkIfAbsent(productId, categoryId);
        verifyNoMoreInteractions(centralAudit);
    }

    @Test
    void unlink_delegatesToLinker() {
        long productId = 2L;
        long categoryId = 1L;

        when(productCategoryRepo.deleteByProduct_ProductIdAndCategory_CategoryId(productId, categoryId))
                .thenReturn(1);

        int result = productWriter.unlink(productId, categoryId);

        assertEquals(1, result);
        verify(productCategoryRepo, times(1))
                .deleteByProduct_ProductIdAndCategory_CategoryId(productId, categoryId);
        verifyNoMoreInteractions(centralAudit);
    }

    @Test
    void exists_delegatesToRepo() {
        when(productRepo.existsByProductNameIgnoreCase("test")).thenReturn(true);

        boolean result = productWriter.exists("test");

        assertTrue(result);
        verify(productRepo, times(1)).existsByProductNameIgnoreCase("test");
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