package commerse.eshop.core.repository;

import commerse.eshop.core.model.entity.Product;
import commerse.eshop.core.model.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductCategoryRepo extends JpaRepository<ProductCategory, Long> {

    // == Fetch all products by category ==
    @Query(value = "select p.* from products p join product_category pc on pc.product_id = p.product_id where pc.category_id = :categoryId" +
            "order by p.createdAt desc", nativeQuery = true)
    List<Product> findProductsByCategory(@Param("categoryId") long categoryId);

    // == Check if link already exists ==
    boolean existsByProduct_ProductIdAndCategory_CategoryId(long productId, long categoryId);

    // == Delete a link if it exists ==
    int deleteByProduct_ProductIdAndCategory_CategoryId(long productId, long categoryId);
}
