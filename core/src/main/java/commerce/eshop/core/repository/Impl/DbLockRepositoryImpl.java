package commerce.eshop.core.repository.Impl;

import commerce.eshop.core.repository.DbLockRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.UUID;

@Repository
public class DbLockRepositoryImpl implements DbLockRepository {

    private final EntityManager em;

    @Autowired
    public DbLockRepositoryImpl(EntityManager em){
        this.em = em;
    }

    @Override
    public boolean tryLockCart(UUID cartId) {
        long key = ByteBuffer.wrap(
                ByteBuffer.allocate(16).putLong(cartId.getMostSignificantBits()).putLong(cartId.getLeastSignificantBits()).array()
        ).getLong(); // or any stable mapping to BIGINT

        Boolean got = (Boolean) em.createNativeQuery("SELECT pg_try_advisory_xact_lock(:k)")
                .setParameter("k", key)
                .getSingleResult();
        return Boolean.TRUE.equals(got);
    }
}
