package commerce.eshop.core.tests.application.categoryTest.factory;

import commerce.eshop.core.application.category.factory.CategoryFactory;
import commerce.eshop.core.model.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoryFactoryTest {

    private CategoryFactory categoryFactory;

    @BeforeEach
    void setUp() {
        categoryFactory = new CategoryFactory();
    }

    @Test
    void handle_success() {
        String name = "Electronics";
        String desc = "Devices and gadgets";

        Category result = categoryFactory.handle(name, desc);

        assertNotNull(result);
        assertEquals(name, result.getCategoryName());
        assertEquals(desc, result.getCategoryDescription());
    }
}