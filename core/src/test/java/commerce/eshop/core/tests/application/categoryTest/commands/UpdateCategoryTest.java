package commerce.eshop.core.tests.application.categoryTest.commands;

import commerce.eshop.core.application.category.commands.UpdateCategory;
import commerce.eshop.core.application.category.writer.CategoryWriter;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.model.entity.Category;
import commerce.eshop.core.web.dto.requests.Category.DTOUpdateCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateCategoryTest {

    private DomainLookupService domainLookupService;
    private CategoryWriter categoryWriter;
    private UpdateCategory updateCategory;

    @BeforeEach
    void setUp() {
        domainLookupService = mock(DomainLookupService.class);
        categoryWriter = mock(CategoryWriter.class);

        updateCategory = new UpdateCategory(domainLookupService, categoryWriter);
    }

    @Test
    void handle_updateBothFields_success() {
        long catId = 10L;
        DTOUpdateCategory dto = new DTOUpdateCategory("NewName", "NewDesc");
        Category existing = mock(Category.class);
        Category saved = mock(Category.class);

        when(domainLookupService.getCategoryOrThrow(catId, EndpointsNameMethods.CATEGORY_UPDATE))
                .thenReturn(existing);
        when(categoryWriter.save(existing, EndpointsNameMethods.CATEGORY_UPDATE))
                .thenReturn(saved);

        Category result = updateCategory.handle(dto, catId);

        assertSame(saved, result);

        verify(existing, times(1)).setCategoryName("NewName");
        verify(existing, times(1)).setCategoryDescription("NewDesc");
        verify(categoryWriter, times(1))
                .save(existing, EndpointsNameMethods.CATEGORY_UPDATE);
    }

    @Test
    void handle_updateNameOnly_success() {
        long catId = 20L;
        DTOUpdateCategory dto = new DTOUpdateCategory("UpdatedName", "");
        Category existing = mock(Category.class);
        Category saved = mock(Category.class);

        when(domainLookupService.getCategoryOrThrow(catId, EndpointsNameMethods.CATEGORY_UPDATE))
                .thenReturn(existing);
        when(categoryWriter.save(existing, EndpointsNameMethods.CATEGORY_UPDATE))
                .thenReturn(saved);

        Category result = updateCategory.handle(dto, catId);

        assertSame(saved, result);

        verify(existing, times(1)).setCategoryName("UpdatedName");
        verify(existing, never()).setCategoryDescription(anyString());
    }

    @Test
    void handle_updateDescOnly_success() {
        long catId = 30L;
        DTOUpdateCategory dto = new DTOUpdateCategory(null, "UpdatedDesc");
        Category existing = mock(Category.class);
        Category saved = mock(Category.class);

        when(domainLookupService.getCategoryOrThrow(catId, EndpointsNameMethods.CATEGORY_UPDATE))
                .thenReturn(existing);
        when(categoryWriter.save(existing, EndpointsNameMethods.CATEGORY_UPDATE))
                .thenReturn(saved);

        Category result = updateCategory.handle(dto, catId);

        assertSame(saved, result);

        verify(existing, never()).setCategoryName(anyString());
        verify(existing, times(1)).setCategoryDescription("UpdatedDesc");
    }

    @Test
    void handle_noChanges_success() {
        long catId = 40L;

        DTOUpdateCategory dto = new DTOUpdateCategory(null, "   "); // blank description

        Category existing = mock(Category.class);
        Category saved = mock(Category.class);

        when(domainLookupService.getCategoryOrThrow(catId, EndpointsNameMethods.CATEGORY_UPDATE))
                .thenReturn(existing);

        when(categoryWriter.save(existing, EndpointsNameMethods.CATEGORY_UPDATE))
                .thenReturn(saved);

        Category result = updateCategory.handle(dto, catId);

        assertSame(saved, result);

        verify(existing, never()).setCategoryName(anyString());
        verify(existing, never()).setCategoryDescription(anyString());
    }

    @Test
    void handle_lookupThrows_propagate() {
        long catId = 50L;
        DTOUpdateCategory dto = new DTOUpdateCategory("Name", "Desc");

        when(domainLookupService.getCategoryOrThrow(catId, EndpointsNameMethods.CATEGORY_UPDATE))
                .thenThrow(new IllegalStateException("not found"));
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> updateCategory.handle(dto, catId)
        );

        assertEquals("not found", ex.getMessage());

        verify(categoryWriter, never()).save(any(), anyString());
    }

    @Test
    void handle_writerThrows_propagate() {
        long catId = 60L;
        DTOUpdateCategory dto = new DTOUpdateCategory("A", "B");
        Category existing = mock(Category.class);

        when(domainLookupService.getCategoryOrThrow(catId, EndpointsNameMethods.CATEGORY_UPDATE))
                .thenReturn(existing);

        doThrow(new RuntimeException("db fail"))
                .when(categoryWriter)
                .save(existing, EndpointsNameMethods.CATEGORY_UPDATE);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> updateCategory.handle(dto, catId)
        );

        assertEquals("db fail", ex.getMessage());
    }
}