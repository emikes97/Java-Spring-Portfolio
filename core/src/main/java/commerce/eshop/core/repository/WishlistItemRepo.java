package commerce.eshop.core.repository;

import commerce.eshop.core.model.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistItemRepo extends JpaRepository<WishlistItem, Long> {


}
