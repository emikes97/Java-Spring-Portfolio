package commerce.eshop.core.tests.application.categoryTest.validation;

import commerce.eshop.core.application.category.validation.AuditedCategoryValidation;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

class AuditedCategoryValidationTest {

    private CentralAudit centralAudit;
    private AuditedCategoryValidation auditedCategoryValidation;

    @BeforeEach
    void setUp() {
        centralAudit = mock(CentralAudit.class);

        auditedCategoryValidation = new AuditedCategoryValidation(centralAudit);
        mockAuditReturnSame(centralAudit);
    }

    @Test
    void checkIfCategoryExists_notDuplicate_noAudit() {
        assertDoesNotThrow(() -> auditedCategoryValidation.checkIfCategoryExists(false));

        verify(centralAudit, never())
                .audit(any(RuntimeException.class), any(), anyString(), any(AuditingStatus.class), anyString());
    }

    @Test
    void checkIfCategoryExists_duplicate_throwsAudited() {
        DuplicateKeyException ex = assertThrows(
                DuplicateKeyException.class,
                () -> auditedCategoryValidation.checkIfCategoryExists(true)
        );

        assertEquals("Category already exists", ex.getMessage());

        verify(centralAudit, times(1)).audit(
                any(DuplicateKeyException.class),
                isNull(),
                eq(EndpointsNameMethods.CATEGORY_CREATE),
                eq(AuditingStatus.ERROR),
                eq("Category already exists")
        );
    }
    // == Private methods ==
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