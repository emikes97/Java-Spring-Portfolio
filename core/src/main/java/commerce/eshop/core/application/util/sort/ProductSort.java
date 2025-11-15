package commerce.eshop.core.application.util.sort;

import java.util.Map;

public final class ProductSort {

    private ProductSort(){}

    // == Whitelisting & Constraints
    /** For DTOProductResponse */
    public static final Map<String, String> PRODUCT_SORT_WHITELIST = Map.ofEntries(
            Map.entry("id", "productId"),
            Map.entry("name", "productName"),
            Map.entry("price", "productPrice")
    );

    public static final int MAX_PAGE_SIZE = 25;
}
