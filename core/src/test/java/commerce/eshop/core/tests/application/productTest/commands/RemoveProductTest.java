package commerce.eshop.core.tests.application.productTest.commands;

import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.product.commands.RemoveProduct;
import commerce.eshop.core.application.product.writer.ProductWriter;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.model.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RemoveProductTest {

    // == Fields ==
    private DomainLookupService domainLookupService;
    private ProductWriter productWriter;
    private RemoveProduct removeProduct;

    // == Tests ==

    @BeforeEach
    void setUp() {
        domainLookupService = mock(DomainLookupService.class);
        productWriter = mock(ProductWriter.class);

        removeProduct = new RemoveProduct(domainLookupService, productWriter);
    }

    @Test
    void handle_success() {
        long id = 10L;
        Product product = mock(Product.class);

        when(domainLookupService.getProductOrThrow(id, EndpointsNameMethods.PRODUCT_REMOVE))
                .thenReturn(product);

        removeProduct.handle(id);

        verify(domainLookupService, times(1))
                .getProductOrThrow(id, EndpointsNameMethods.PRODUCT_REMOVE);
        verify(productWriter, times(1)).delete(product);
        verifyNoMoreInteractions(domainLookupService, productWriter);
    }
}