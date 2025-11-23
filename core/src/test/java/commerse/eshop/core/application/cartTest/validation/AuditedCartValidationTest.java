package commerse.eshop.core.application.cartTest.validation;


import commerce.eshop.core.application.cart.validation.AuditedCartValidation;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuditedCartValidationTest {

    private CentralAudit centralAudit;
    private AuditedCartValidation auditedCartValidation;

    @BeforeEach
    void setUp() {
        centralAudit = mock(CentralAudit.class);

        auditedCartValidation = new AuditedCartValidation(centralAudit);
        mockAuditReturnSame(centralAudit);
    }

    @Test
    void checkValidQuantity_successNoThrow() {
        UUID customerId = UUID.randomUUID();
        int quantity = 5;
        int max = 99;

        assertDoesNotThrow(() -> auditedCartValidation.checkValidQuantity(quantity, customerId, max));

        verifyNoInteractions(centralAudit);
    }

    @Test
    void checkValidQuantity_throws(){
        UUID customerId = UUID.randomUUID();
        int quantity = 0;
        int max = 99;

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> auditedCartValidation.checkValidQuantity(quantity, customerId, max)
        );

        assertEquals("Quantity must be positive, and shouldn't exceed 99", ex.getMessage());

        verify(centralAudit, times(1)).audit(
                any(IllegalArgumentException.class),
                eq(customerId),
                eq(EndpointsNameMethods.CART_ADD_ITEM),
                eq(AuditingStatus.ERROR)
        );
    }

    @Test
    void testCheckValidQuantity_successPathNoThrow_twoArgs() {
        UUID customerId = UUID.randomUUID();
        int quantity = 3;

        assertDoesNotThrow(() -> auditedCartValidation.checkValidQuantity(quantity, customerId));

        verifyNoInteractions(centralAudit);
    }

    @Test
    void testCheckValidQuantity_throwPath_twoArgs(){
        UUID customerId = UUID.randomUUID();
        int quantity = -1;

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> auditedCartValidation.checkValidQuantity(quantity, customerId)
        );

        assertEquals("Quantity must be positive.", ex.getMessage());

        verify(centralAudit, times(1)).audit(
                any(IllegalArgumentException.class),
                eq(customerId),
                eq(EndpointsNameMethods.CART_REMOVE),
                eq(AuditingStatus.ERROR)
        );
    }

    // == Private Methods ==
    static <E extends RuntimeException> void mockAuditReturnSame(CentralAudit auditMock) {
        when(auditMock.audit(
                any(RuntimeException.class),
                any(UUID.class),
                anyString(),
                any(AuditingStatus.class)
        )).thenAnswer(inv -> inv.getArgument(0));
    }
}