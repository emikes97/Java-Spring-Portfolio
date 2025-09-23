package commerce.eshop.core.repository;

import java.util.UUID;

public interface DbLockRepository {
    boolean tryLockCart(UUID cartId);
}
