package commerse.eshop.core.repository;

import commerse.eshop.core.model.entity.Product;
import commerse.eshop.core.model.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductCategoryRepo extends JpaRepository<ProductCategory, Long> {

    // == Check if link already exists ==
    boolean existsByProduct_ProductIdAndCategory_CategoryId(long productId, long categoryId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    INSERT INTO product_category (product_id, category_id)
    VALUES (:productId, :categoryId)
    ON CONFLICT (product_id, category_id) DO NOTHING
    """, nativeQuery = true)
    int linkIfAbsent(@Param("productId") long productId, @Param("categoryId") long categoryId);

    // == Delete a link if it exists ==
    int deleteByProduct_ProductIdAndCategory_CategoryId(long productId, long categoryId);
}
