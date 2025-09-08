package commerce.eshop.core.repository;

import commerce.eshop.core.model.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WishlistRepo extends JpaRepository<Wishlist, UUID> {
}
