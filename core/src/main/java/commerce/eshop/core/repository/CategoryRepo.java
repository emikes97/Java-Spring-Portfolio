package commerce.eshop.core.repository;

import commerce.eshop.core.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepo extends JpaRepository<Category, Long> {

    // == Find Category by Name
    boolean existsByCategoryNameIgnoreCase(String categoryName); // Ignore case

    // == Delete by ID
    @Modifying
    @Query(value = "delete from categories where category_id = :catId", nativeQuery = true)
    int deleteCategory(@Param("catId") long catId);
}
