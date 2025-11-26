package commerce.eshop.core.tests.application.productTest.commands;

import commerce.eshop.core.application.product.commands.AddProduct;
import commerce.eshop.core.application.product.factory.ProductFactory;
import commerce.eshop.core.application.product.validation.AuditedProductValidation;
import commerce.eshop.core.application.product.writer.ProductWriter;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.web.dto.requests.Products.DTOAddProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddProductTest {

    // == Fields ==
    private AuditedProductValidation auditedProductValidation;
    private ProductFactory productFactory;
    private ProductWriter productWriter;
    private AddProduct addProduct;

    // == Tests ==

    @BeforeEach
    void setUp() {
        auditedProductValidation = mock(AuditedProductValidation.class);
        productFactory = mock(ProductFactory.class);
        productWriter = mock(ProductWriter.class);

        addProduct = new AddProduct(auditedProductValidation, productFactory, productWriter);
    }

    @Test
    void handle_success() {
        DTOAddProduct dto = new DTOAddProduct(
                "  Name  ",
                "  Description  ",
                Map.of("k", "v"),
                10,
                new BigDecimal("5.50"),
                true
        );
        String normalizedName = "Name";
        String normalizedDesc = "Description";
        Product created = mock(Product.class);
        Product saved = mock(Product.class);

        when(productFactory.handle(normalizedName, normalizedDesc, dto)).thenReturn(created);
        when(productWriter.save(created, EndpointsNameMethods.PRODUCT_ADD)).thenReturn(saved);

        Product result = addProduct.handle(dto);

        assertSame(saved, result);

        // Order-wise we just assert they were called with the right arguments
        verify(auditedProductValidation, times(1)).checkData(dto);
        verify(auditedProductValidation, times(1)).checkIfProductExists(normalizedName);
        verify(productFactory, times(1)).handle(normalizedName, normalizedDesc, dto);
        verify(productWriter, times(1)).save(created, EndpointsNameMethods.PRODUCT_ADD);

        verifyNoMoreInteractions(auditedProductValidation, productFactory, productWriter);
    }
}