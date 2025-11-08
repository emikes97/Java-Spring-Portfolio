package commerce.eshop.core.application.email.templating;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tiny fluent builder for template variables.
 * Usage:
 *   Map<String,Object> vars = Vars.of()
 *       .put("customerName", name)
 *       .put("orderId", orderId)
 *       .build();
 *
 * Pass the resulting map to your TemplateEngine.render(...).
 */

public class Vars {

    // Backing mutable map that accumulates all variables you add with put(...).
    // We use Object as the value type so callers can pass any type (UUID, BigDecimal, etc.).
    private final Map<String,Object> map = new HashMap<>();

    // Static factory: start a new builder.
    // Prefer this over "new Vars()" for readability at the call site.
    public static Vars of() { return new Vars(); }

    // Add one key/value pair and return "this" so calls can be chained:
    // Vars.of().put("a",1).put("b",2)...
    // Note: null values are allowed here; your TemplateEngine converts null -> "" during rendering.
    public Vars put(String k, Object v) { map.put(k, v); return this; }

    // Return an *unmodifiable view* of the current map.
    // Important: this is NOT a snapshot; if you keep using this builder after build(),
    // the returned map will reflect later changes because it's a read-only wrapper
    // around the same underlying "map".
    //
    // If you ever need a true snapshot, you can replace this with:
    //   return new HashMap<>(map);            // mutable snapshot
    // or (if you guarantee no nulls):
    //   return Map.copyOf(map);               // immutable snapshot (disallows nulls)
    public Map<String,Object> build() { return Collections.unmodifiableMap(map); }
}
