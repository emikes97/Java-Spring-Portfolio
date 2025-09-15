package commerce.eshop.core.repository;

import commerce.eshop.core.model.entity.EmailsSent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EmailsSentRepo extends JpaRepository<EmailsSent, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
      WITH picked AS (
        SELECT email_id
        FROM emails_sent
        WHERE status = 'QUEUED'
        ORDER BY created_at
        FOR UPDATE SKIP LOCKED
        LIMIT :batch
      )
      UPDATE emails_sent e
         SET status = 'SENDING'
        FROM picked
       WHERE e.email_id = picked.email_id
      RETURNING e.*;
      """, nativeQuery = true)
    List<EmailsSent> claimBatch(@Param("batch") int batch);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    UPDATE emails_sent
       SET status  = 'SENT'::email_status,
           sent_at = now()
     WHERE email_id = :id
    """, nativeQuery = true)
    int markSent(@Param("id") UUID id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    UPDATE emails_sent
       SET status = 'FAILED'::email_status
     WHERE email_id = :id
    """, nativeQuery = true)
    int markFailed(@Param("id") UUID id);
}
