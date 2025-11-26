package commerce.eshop.core.tests.application.productTest.factory;

import commerce.eshop.core.application.product.factory.ProductFactory;
import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.web.dto.requests.Products.DTOAddProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProductFactoryTest {

    // == Fields ==
    private ProductFactory productFactory;

    @BeforeEach
    void setUp() {
        productFactory = new ProductFactory();
    }

    @Test
    void handle_createsProductCorrectly() {
        String normalizedName = "Normalized Name";
        String normalizedDesc = "Normalized Description";

        DTOAddProduct dto = new DTOAddProduct(
                "Original Name",
                "Original Desc",
                Map.of("key", "value"),
                50,
                new BigDecimal("99.99"),
                true
        );

        Product result = productFactory.handle(normalizedName, normalizedDesc, dto);

        assertNotNull(result);
        assertEquals(normalizedName, result.getProductName());
        assertEquals(normalizedDesc, result.getDescription());
        assertEquals(dto.productDetails(), result.getProductDetails());
        assertEquals(dto.productAvailableStock(), result.getProductAvailableStock());
        assertEquals(dto.productPrice(), result.getPrice());
        assertTrue(result.isActive());
    }

    @Test
    void normaliseNewProduct_withProvidedFields_createsDTO() {
        Map<String, Object> item = Map.of(
                "title", "Sample Product",
                "description", "Cool item",
                "price", 12.50
        );

        DTOAddProduct dto = productFactory.normaliseNewProduct(item);

        assertNotNull(dto);
        assertEquals("Sample Product", dto.productName());
        assertEquals("Cool item", dto.productDescription());
        assertEquals(item, dto.productDetails());
        assertEquals(BigDecimal.valueOf(12.50), dto.productPrice());
        assertTrue(dto.isActive());
        assertTrue(dto.productAvailableStock() >= 10 && dto.productAvailableStock() < 100);
    }
}