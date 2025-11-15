package commerce.eshop.core.application.product.queries;

import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.service.DomainLookupService;
import commerce.eshop.core.util.SortSanitizer;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.sort.ProductSort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProductQueries {
    // == Fields ==
    private final DomainLookupService domainLookupService;
    private final SortSanitizer sortSanitizer;

    // == Constructors ==
    @Autowired
    public ProductQueries(DomainLookupService domainLookupService, SortSanitizer sortSanitizer) {
        this.domainLookupService = domainLookupService;
        this.sortSanitizer = sortSanitizer;
    }

    // == Public Methods ==
    @Transactional(readOnly = true)
    public Product getProduct(long id){
        return domainLookupService.getProductOrThrow(id, EndpointsNameMethods.PRODUCT_GET);
    }

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(long categoryId, Pageable pageable){
        Pageable p = sortSanitizer.sanitize(pageable, ProductSort.PRODUCT_SORT_WHITELIST, ProductSort.MAX_PAGE_SIZE);
        return domainLookupService.getPagedProducts(categoryId,p);
    }
}
