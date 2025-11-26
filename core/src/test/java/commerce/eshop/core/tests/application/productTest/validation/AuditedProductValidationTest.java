package commerce.eshop.core.tests.application.productTest.validation;

import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.product.validation.AuditedProductValidation;
import commerce.eshop.core.application.product.writer.ProductWriter;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.web.dto.requests.Products.DTOAddProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuditedProductValidationTest {

    // == Fields ==
    private ProductWriter productWriter;
    private CentralAudit centralAudit;
    private AuditedProductValidation auditedProductValidation;

    // == Tests ==

    @BeforeEach
    void setUp() {
        productWriter = mock(ProductWriter.class);
        centralAudit = mock(CentralAudit.class);

        auditedProductValidation = new AuditedProductValidation(productWriter, centralAudit);
        mockAuditReturnSame(centralAudit);
    }

    @Test
    void checkData_nullDto_throw() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> auditedProductValidation.checkData(null)
        );

        assertEquals("Product Details can't be empty", ex.getMessage());

        verify(centralAudit, times(1)).audit(
                any(IllegalArgumentException.class),
                isNull(),
                eq(EndpointsNameMethods.PRODUCT_ADD),
                eq(AuditingStatus.WARNING),
                anyString()
        );
    }

    @Test
    void checkData_validDto() {
        DTOAddProduct dto = new DTOAddProduct(
                "Name",
                "Desc",
                Map.of("k", "v"),
                10,
                new BigDecimal("1.00"),
                true
        );

        assertDoesNotThrow(() -> auditedProductValidation.checkData(dto));

        verifyNoMoreInteractions(centralAudit);
    }

    @Test
    void checkIfProductExists_failAlreadyExists() {
        String name = "NormalizedName";

        when(productWriter.exists(name)).thenReturn(true);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> auditedProductValidation.checkIfProductExists(name)
        );

        assertEquals("Product already exists", ex.getMessage());

        verify(productWriter, times(1)).exists(name);
        verify(centralAudit, times(1)).audit(
                any(IllegalStateException.class),
                isNull(),
                eq(EndpointsNameMethods.PRODUCT_ADD),
                eq(AuditingStatus.WARNING),
                anyString()
        );
    }


    @Test
    void checkIfProductExists_successNoThrow() {
        String name = "NormalizedName";

        when(productWriter.exists(name)).thenReturn(false);

        assertDoesNotThrow(() -> auditedProductValidation.checkIfProductExists(name));

        verify(productWriter, times(1)).exists(name);
        verifyNoMoreInteractions(centralAudit);
    }

    @Test
    void checkIfQuantityIsValid_nonPositive_throw() {
        IllegalArgumentException exZero = assertThrows(
                IllegalArgumentException.class,
                () -> auditedProductValidation.checkIfQuantityIsValid(0)
        );
        assertEquals("Quantity can't be negative/or zero for increasing the stock", exZero.getMessage());

        verify(centralAudit, times(1)).audit(
                any(IllegalArgumentException.class),
                isNull(),
                eq(EndpointsNameMethods.PRODUCT_INCREASE_QTY),
                eq(AuditingStatus.WARNING),
                anyString()
        );
    }

    @Test
    void checkIfQuantityIsValid_positive_successful() {
        assertDoesNotThrow(() -> auditedProductValidation.checkIfQuantityIsValid(5));

        verifyNoMoreInteractions(centralAudit);
    }

    @Test
    void checkAvailableQuantity_notEnough_throw() {
        int available = 5;
        int requested = 10;

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> auditedProductValidation.checkAvailableQuantity(available, requested)
        );

        assertEquals("Insufficient stock: available=5, requested=10", ex.getMessage());

        verify(centralAudit, times(1)).audit(
                any(IllegalStateException.class),
                isNull(),
                eq(EndpointsNameMethods.PRODUCT_DECREASE_QTY),
                eq(AuditingStatus.WARNING),
                anyString()
        );
    }

    @Test
    void checkAvailableQuantity_enough_successful() {
        assertDoesNotThrow(() -> auditedProductValidation.checkAvailableQuantity(10, 5));

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