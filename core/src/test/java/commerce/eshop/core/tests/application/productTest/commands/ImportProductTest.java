package commerce.eshop.core.tests.application.productTest.commands;

import commerce.eshop.core.application.async.external.contracts.ImportClientProduct;
import commerce.eshop.core.application.product.commands.ImportProduct;
import commerce.eshop.core.application.product.factory.ProductFactory;
import commerce.eshop.core.application.product.validation.AuditedProductValidation;
import commerce.eshop.core.application.product.writer.ProductWriter;
import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.web.dto.requests.Products.DTOAddProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImportProductTest {

    // == Fields ==
    private ProductFactory productFactory;
    private ProductWriter productWriter;
    private AuditedProductValidation auditedProductValidation;
    private ImportClientProduct importClientProduct;
    private ImportProduct importProduct;

    // == Tests ==

    @BeforeEach
    void setUp() {
        productFactory = mock(ProductFactory.class);
        productWriter = mock(ProductWriter.class);
        auditedProductValidation = mock(AuditedProductValidation.class);
        importClientProduct = mock(ImportClientProduct.class);

        importProduct = new ImportProduct(productFactory, productWriter, auditedProductValidation, importClientProduct);
    }

    @Test
    void handle_successFlow() {
        Map<String, Object> raw = Map.of(
                "title", "External Product",
                "description", "From API",
                "price", 19.99
        );
        DTOAddProduct dto = new DTOAddProduct(
                "External Product",
                "From API",
                raw,
                25,
                BigDecimal.valueOf(19.99),
                true
        );
        Product productToSave = mock(Product.class);
        Product saved = mock(Product.class);

        when(importClientProduct.getProduct()).thenReturn(raw);
        when(productFactory.normaliseNewProduct(raw)).thenReturn(dto);
        when(productFactory.handle(dto.productName(), dto.productDescription(), dto))
                .thenReturn(productToSave);
        when(productToSave.getProductName()).thenReturn(dto.productName());
        when(productWriter.save(productToSave, "ImportNewProduct")).thenReturn(saved);

        Product result = importProduct.handle();

        assertSame(saved, result);

        verify(importClientProduct, times(1)).getProduct();
        verify(productFactory, times(1)).normaliseNewProduct(raw);
        verify(productFactory, times(1))
                .handle(dto.productName(), dto.productDescription(), dto);
        verify(auditedProductValidation, times(1))
                .checkIfProductExists(dto.productName());
        verify(productWriter, times(1))
                .save(productToSave, "ImportNewProduct");
        verifyNoMoreInteractions(productWriter, productFactory, auditedProductValidation, importClientProduct);
    }
}