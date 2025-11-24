package commerce.eshop.core.tests.application.categoryTest.writer;

import commerce.eshop.core.application.category.writer.CategoryWriter;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.model.entity.Category;
import commerce.eshop.core.repository.CategoryRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CategoryWriterTest {

    private CategoryRepo categoryRepo;
    private CentralAudit centralAudit;
    private CategoryWriter categoryWriter;

    @BeforeEach
    void setUp() {
        categoryRepo = mock(CategoryRepo.class);
        centralAudit = mock(CentralAudit.class);

        categoryWriter = new CategoryWriter(categoryRepo, centralAudit);
        mockAuditReturnSame(centralAudit);
    }

    @Test
    void save_success() {
        Category cat = mock(Category.class);
        Category saved = mock(Category.class);

        when(categoryRepo.saveAndFlush(cat)).thenReturn(saved);

        Category result = categoryWriter.save(cat, "EP");

        assertSame(saved, result);
        verify(categoryRepo, times(1)).saveAndFlush(cat);
        verify(centralAudit, never()).audit(any(), any(), anyString(), any(), anyString());
    }

    @Test
    void save_constraintViolation_throw() {
        Category cat = mock(Category.class);
        DataIntegrityViolationException ex = new DataIntegrityViolationException("dup");

        when(categoryRepo.saveAndFlush(cat)).thenThrow(ex);

        DataIntegrityViolationException thrown = assertThrows(
                DataIntegrityViolationException.class,
                () -> categoryWriter.save(cat, "EP")
        );

        assertSame(ex, thrown);
        verify(centralAudit, times(1))
                .audit(eq(ex), isNull(), eq("EP"), eq(AuditingStatus.ERROR), anyString());
    }

    @Test
    void delete_success() {
        categoryWriter.delete(10L, "EP");

        verify(categoryRepo, times(1)).deleteCategory(10L);
        verify(categoryRepo, times(1)).flush();
        verify(centralAudit, never()).audit(any(), any(), anyString(), any(), anyString());
    }

    @Test
    void delete_notFound_throw() {
        NoSuchElementException ex = new NoSuchElementException("not found");

        doThrow(ex).when(categoryRepo).deleteCategory(5L);

        NoSuchElementException thrown = assertThrows(
                NoSuchElementException.class,
                () -> categoryWriter.delete(5L, "EP")
        );

        assertSame(ex, thrown);
        verify(centralAudit, times(1))
                .audit(eq(ex), isNull(), eq("EP"), eq(AuditingStatus.ERROR), anyString());
    }

    @Test
    void delete_constraintViolation_throw() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("fk");

        doThrow(ex).when(categoryRepo).flush();             // flush fails

        DataIntegrityViolationException thrown = assertThrows(
                DataIntegrityViolationException.class,
                () -> categoryWriter.delete(7L, "EP")
        );

        assertSame(ex, thrown);
        verify(centralAudit, times(1))
                .audit(eq(ex), isNull(), eq("EP"), eq(AuditingStatus.ERROR), anyString());
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