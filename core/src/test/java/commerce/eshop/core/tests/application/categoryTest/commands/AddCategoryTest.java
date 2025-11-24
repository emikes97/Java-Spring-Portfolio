package commerce.eshop.core.tests.application.categoryTest.commands;

import commerce.eshop.core.application.category.commands.AddCategory;
import commerce.eshop.core.application.category.factory.CategoryFactory;
import commerce.eshop.core.application.category.validation.AuditedCategoryValidation;
import commerce.eshop.core.application.category.writer.CategoryWriter;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.model.entity.Category;
import commerce.eshop.core.web.dto.requests.Category.DTOAddCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddCategoryTest {

    private DomainLookupService domainLookupService;
    private AuditedCategoryValidation auditedCategoryValidation;
    private CategoryFactory categoryFactory;
    private CategoryWriter categoryWriter;
    private AddCategory addCategory;

    @BeforeEach
    void setUp() {
        domainLookupService = mock(DomainLookupService.class);
        auditedCategoryValidation = mock(AuditedCategoryValidation.class);
        categoryFactory = mock(CategoryFactory.class);
        categoryWriter = mock(CategoryWriter.class);

        addCategory = new AddCategory(domainLookupService, auditedCategoryValidation, categoryFactory, categoryWriter);
    }


    @Test
    void handle_success() {
        DTOAddCategory dto = new DTOAddCategory("Electronics", "Devices");
        Category newCat = mock(Category.class);
        Category savedCat = mock(Category.class);

        when(domainLookupService.checkIfCatExists("Electronics")).thenReturn(false);
        when(categoryFactory.handle("Electronics", "Devices")).thenReturn(newCat);
        when(categoryWriter.save(newCat, EndpointsNameMethods.CATEGORY_CREATE))
                .thenReturn(savedCat);

        Category result = addCategory.handle(dto);

        assertSame(savedCat, result);

        verify(domainLookupService, times(1)).checkIfCatExists("Electronics");
        verify(auditedCategoryValidation, times(1)).checkIfCategoryExists(false);
        verify(categoryFactory, times(1)).handle("Electronics", "Devices");
        verify(categoryWriter, times(1)).save(newCat, EndpointsNameMethods.CATEGORY_CREATE);
    }

    @Test
    void handle_duplicate_throws() {
        DTOAddCategory dto = new DTOAddCategory("Phones", "Smart devices");

        when(domainLookupService.checkIfCatExists("Phones")).thenReturn(true);

        doThrow(new IllegalArgumentException("duplicate"))
                .when(auditedCategoryValidation)
                .checkIfCategoryExists(true);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> addCategory.handle(dto)
        );

        assertEquals("duplicate", ex.getMessage());

        verify(domainLookupService, times(1)).checkIfCatExists("Phones");
        verify(auditedCategoryValidation, times(1)).checkIfCategoryExists(true);
        verify(categoryFactory, never()).handle(anyString(), anyString());
        verify(categoryWriter, never()).save(any(Category.class), anyString());
    }
}