package commerce.eshop.core.util.sort;

import commerce.eshop.core.model.entity.Cart;

import java.util.Map;

public final class CartSort {

    private CartSort(){}

    // == Whitelisting & Constraints
    public static final Map<String, String> CART_ITEMS_SORT_WHITELIST = Map.ofEntries(
            Map.entry("added_at",   "addedAt"),
            Map.entry("quantity",   "quantity"),
            Map.entry("unit_price", "priceAt"),
            Map.entry("product_name","productName")
    );

    public static final int MAX_PAGE_SIZE = 25;
}
