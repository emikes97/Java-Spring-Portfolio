package commerce.eshop.core.tests.application.categoryTest.commands;

import commerce.eshop.core.application.category.commands.DeleteCategory;
import commerce.eshop.core.application.category.writer.CategoryWriter;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DeleteCategoryTest {

    private CategoryWriter categoryWriter;
    private DeleteCategory deleteCategory;

    @BeforeEach
    void setUp() {
        categoryWriter = mock(CategoryWriter.class);

        deleteCategory = new DeleteCategory(categoryWriter);
    }


    @Test
    void handle_success() {
        deleteCategory.handle(5L);

        verify(categoryWriter, times(1))
                .delete(eq(5L), eq(EndpointsNameMethods.CATEGORY_DELETE));
    }

    @Test
    void handle_writerThrows_propagate() {
        doThrow(new IllegalStateException("boom"))
                .when(categoryWriter)
                .delete(eq(99L), eq(EndpointsNameMethods.CATEGORY_DELETE));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> deleteCategory.handle(99L)
        );

        assertEquals("boom", ex.getMessage());
    }
}