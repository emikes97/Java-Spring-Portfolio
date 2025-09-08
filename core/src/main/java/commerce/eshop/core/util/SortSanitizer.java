package commerce.eshop.core.util;

import commerce.eshop.core.model.entity.Auditing;
import commerce.eshop.core.service.AuditingService;
import commerce.eshop.core.util.enums.AuditingStatus;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SortSanitizer {

    // == Fields ==
    private static final  int DEFAULT_MAX_PAGE_SIZE = 100;
    private final AuditingService auditingService;

    // == Constructors ==
    public SortSanitizer(AuditingService auditingService){
        this.auditingService = auditingService;
    }

    // == Public Methods ==
    /**
     * Validates client sort keys against an allow-list and translates API names (e.g. snake_case)
     * to entity fields (camelCase). Preserves direction, ignore-case, and null-handling.
     * Clamps page size and applies a default sort if the incoming sort is unsorted.
     *
     * @param pageable     incoming pageable (can be null)
     * @param allowedMap   API key -> entity property (e.g., "created_at" -> "createdAt")
     * @param maxPageSize  maximum allowed page size (e.g., 100)
     * @param defaultSort  used only when incoming sort is unsorted; null to keep unsorted
     * @return sanitized Pageable ready for repository calls
     */

    public Pageable sanitize(@Nullable Pageable pageable, Map<String, String> allowedMap, int maxPageSize, @Nullable Sort defaultSort){

        // No pageable provided -> unpaged
        if (pageable == null) return Pageable.unpaged();

        // Unpaged passthrough
        if(pageable.isUnpaged()) return pageable;

        // Clamp page & Size
        int page = Math.max(0, pageable.getPageNumber());
        int size = Math.max(1, Math.min(pageable.getPageSize(), maxPageSize));

        Sort incoming = pageable.getSort();

        // if no sort provided, optionally apply a default
        if (incoming == null || incoming.isUnsorted()){
            return (defaultSort != null)
                    ? PageRequest.of(page, size, defaultSort)
                    : PageRequest.of(page, size);
        }

        // Translate & validate each sort order
        List<Sort.Order> translated = new ArrayList<>();
        for(Sort.Order o : incoming){

            String apiProp = o.getProperty();
            String entityProp = allowedMap.get(apiProp);
            if(entityProp == null) {
                auditingService.log(null, "Sort_Sanitizer_Class", AuditingStatus.ERROR, "Invalid sort property: " + apiProp);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid sort property: " + apiProp);
            }

            // Start with direction + mapped property
            Sort.Order order = new Sort.Order(o.getDirection(), entityProp);

            // Preserve ingore-case
            if(o.isIgnoreCase()){
                order = order.ignoreCase();
            }

            // Preserve null-handling
            switch (o.getNullHandling()){
                case NULLS_FIRST -> order = order.nullsFirst();
                case NULLS_LAST -> order = order.nullsLast();
                case NATIVE -> {/*default case*/}
            }

            translated.add(order);
        }

        return PageRequest.of(page, size, Sort.by(translated));
    }

    /** Convenience overload with no default sort, using a custom max size. */
    public Pageable sanitize(@Nullable Pageable pageable,
                             Map<String, String> allowedMap,
                             int maxPageSize) {
        return sanitize(pageable, allowedMap, maxPageSize, null);
    }

    /** Convenience overload with DEFAULT_MAX_PAGE_SIZE and no default sort. */
    public Pageable sanitize(@Nullable Pageable pageable,
                             Map<String, String> allowedMap) {
        return sanitize(pageable, allowedMap, DEFAULT_MAX_PAGE_SIZE, null);
    }
}
