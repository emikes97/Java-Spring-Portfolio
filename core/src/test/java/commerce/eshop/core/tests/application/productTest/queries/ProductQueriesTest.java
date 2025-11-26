package commerce.eshop.core.tests.application.productTest.queries;

import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.product.queries.ProductQueries;
import commerce.eshop.core.application.util.SortSanitizer;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.sort.ProductSort;
import commerce.eshop.core.model.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductQueriesTest {

    // == Fields ==
    private DomainLookupService domainLookupService;
    private SortSanitizer sortSanitizer;
    private ProductQueries productQueries;

    // == Tests ==

    @BeforeEach
    void setUp() {
        domainLookupService = mock(DomainLookupService.class);
        sortSanitizer = mock(SortSanitizer.class);

        productQueries = new ProductQueries(domainLookupService, sortSanitizer);
    }

    @Test
    void getProduct_delegatesToDomainLookup() {
        long id = 5L;
        Product product = mock(Product.class);

        when(domainLookupService.getProductOrThrow(id, EndpointsNameMethods.PRODUCT_GET))
                .thenReturn(product);

        Product result = productQueries.getProduct(id);

        assertSame(product, result);
        verify(domainLookupService, times(1))
                .getProductOrThrow(id, EndpointsNameMethods.PRODUCT_GET);
        verifyNoInteractions(sortSanitizer);
    }

    @Test
    void getAllProducts_sanitizesAndDelegatesToDomainLookup() {
        long categoryId = 10L;
        Pageable originalPage = mock(Pageable.class);
        Pageable sanitizedPage = mock(Pageable.class);
        Page<Product> items = mock(Page.class);

        when(sortSanitizer.sanitize(
                eq(originalPage),
                eq(ProductSort.PRODUCT_SORT_WHITELIST),
                eq(ProductSort.MAX_PAGE_SIZE)
        )).thenReturn(sanitizedPage);
        when(domainLookupService.getPagedProducts(categoryId, sanitizedPage))
                .thenReturn(items);

        Page<Product> result = productQueries.getAllProducts(categoryId, originalPage);

        assertSame(items, result);

        verify(sortSanitizer, times(1)).sanitize(
                eq(originalPage),
                eq(ProductSort.PRODUCT_SORT_WHITELIST),
                eq(ProductSort.MAX_PAGE_SIZE)
        );
        verify(domainLookupService, times(1))
                .getPagedProducts(categoryId, sanitizedPage);
    }
}