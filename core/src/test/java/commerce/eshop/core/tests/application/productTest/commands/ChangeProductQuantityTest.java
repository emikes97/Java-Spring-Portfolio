package commerce.eshop.core.tests.application.productTest.commands;

import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.product.commands.ChangeProductQuantity;
import commerce.eshop.core.application.product.validation.AuditedProductValidation;
import commerce.eshop.core.application.product.writer.ProductWriter;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.model.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChangeProductQuantityTest {

    // == Fields ==
    private DomainLookupService domainLookupService;
    private AuditedProductValidation auditedProductValidation;
    private ProductWriter productWriter;
    private CentralAudit centralAudit;
    private ChangeProductQuantity changeProductQuantity;

    @BeforeEach
    void setUp() {
        domainLookupService = mock(DomainLookupService.class);
        auditedProductValidation = mock(AuditedProductValidation.class);
        productWriter = mock(ProductWriter.class);
        centralAudit = mock(CentralAudit.class);

        changeProductQuantity = new ChangeProductQuantity(domainLookupService, auditedProductValidation, productWriter, centralAudit);
    }

    @Test
    void increaseQuantity_success() {
        long productId = 1L;
        int quantity = 5;
        Product product = mock(Product.class);

        when(domainLookupService.getProductOrThrow(productId, EndpointsNameMethods.PRODUCT_INCREASE_QTY))
                .thenReturn(product);
        when(product.getProductAvailableStock()).thenReturn(10);

        changeProductQuantity.increaseQuantity(productId, quantity);

        verify(auditedProductValidation, times(1)).checkIfQuantityIsValid(quantity);
        verify(domainLookupService, times(1))
                .getProductOrThrow(productId, EndpointsNameMethods.PRODUCT_INCREASE_QTY);

        // 10 + 5 = 15
        verify(product, times(1)).setProductAvailableStock(15);
        verify(productWriter, times(1)).save(product, EndpointsNameMethods.PRODUCT_INCREASE_QTY);

        verifyNoInteractions(centralAudit);
    }

    @Test
    void decreaseQuantity_success() {
        long productId = 2L;
        int quantity = 3;
        Product product = mock(Product.class);

        when(domainLookupService.getProductOrThrow(productId, EndpointsNameMethods.PRODUCT_DECREASE_QTY))
                .thenReturn(product);
        when(product.getProductAvailableStock()).thenReturn(10);

        changeProductQuantity.decreaseQuantity(productId, quantity);

        verify(domainLookupService, times(1))
                .getProductOrThrow(productId, EndpointsNameMethods.PRODUCT_DECREASE_QTY);
        verify(auditedProductValidation, times(1))
                .checkAvailableQuantity(10, quantity);

        // 10 - 3 = 7
        verify(product, times(1)).setProductAvailableStock(7);
        verify(productWriter, times(1)).save(product, EndpointsNameMethods.PRODUCT_DECREASE_QTY);

        verifyNoInteractions(centralAudit);
    }

    @Test
    void decreaseQuantity_dataIntegrityViolation_isAuditedAndSwallowed() {
        long productId = 3L;
        int quantity = 4;
        Product product = mock(Product.class);
        DataIntegrityViolationException dup = new DataIntegrityViolationException("constraint");

        when(domainLookupService.getProductOrThrow(productId, EndpointsNameMethods.PRODUCT_DECREASE_QTY))
                .thenReturn(product);
        when(product.getProductAvailableStock()).thenReturn(10);
        doThrow(dup).when(productWriter).save(product, EndpointsNameMethods.PRODUCT_DECREASE_QTY);

        assertDoesNotThrow(
                () -> changeProductQuantity.decreaseQuantity(productId, quantity)
        );

        verify(domainLookupService, times(1))
                .getProductOrThrow(productId, EndpointsNameMethods.PRODUCT_DECREASE_QTY);
        verify(auditedProductValidation, times(1))
                .checkAvailableQuantity(10, quantity);
        verify(product, times(1)).setProductAvailableStock(6); // 10 - 4
        verify(productWriter, times(1))
                .save(product, EndpointsNameMethods.PRODUCT_DECREASE_QTY);

        verify(centralAudit, times(1)).audit(
                eq(dup),
                isNull(),
                eq(EndpointsNameMethods.PRODUCT_DECREASE_QTY),
                eq(AuditingStatus.ERROR),
                eq(dup.toString())
        );
    }
}