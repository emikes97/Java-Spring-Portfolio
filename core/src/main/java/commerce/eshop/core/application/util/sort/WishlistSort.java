package commerce.eshop.core.application.util.sort;

import java.util.Map;

public final class WishlistSort {

    private WishlistSort(){}

    // == Whitelist & Constraints ==
    public static final Map<String, String> WISHLIST_SORT_WHITELIST = Map.ofEntries(
            Map.entry("productName", "productName"),
            Map.entry("product_name", "productName"),
            Map.entry("name", "productName"),
            Map.entry("addedAt", "addedAt"),
            Map.entry("added_at", "addedAt"),
            Map.entry("date", "addedAt")
    );

    public static final int MAX_PAGE_SIZE = 25;
}
