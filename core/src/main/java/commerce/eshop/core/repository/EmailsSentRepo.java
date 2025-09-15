package commerce.eshop.core.repository;

import commerce.eshop.core.model.entity.EmailsSent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EmailsSentRepo extends JpaRepository<EmailsSent, UUID> {

    @Query(value = """
  SELECT * FROM emails_sent
  WHERE status = 'QUEUED'
  ORDER BY created_at
  FOR UPDATE SKIP LOCKED
  LIMIT :batch
  """, nativeQuery = true)
    List<EmailsSent> lockQueuedBatch(int batch);

    @Query(value = """
            SELECT * FROM emails_sent
            WHERE status = 'SENDING'
            ORDER BY created_at
            limit :batch""", nativeQuery = true)
    List<EmailsSent> rescueLockedBatch(int batch);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
  UPDATE emails_sent
     SET status = 'SENDING'::email_status
   WHERE email_id = ANY(:ids)
  """, nativeQuery = true)
    int markSendingBatch(UUID[] ids);

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

    @Query(value = "select exists (select 1 from emails_sent where status = 'QUEUED')", nativeQuery = true)
    boolean queuesExists();

    @Query(value = """
    select exists (
      select 1 from emails_sent
       where status='SENDING'
         and created_at < now() - interval '5 minutes'
    )
""", nativeQuery = true)
    boolean hasStuckSending();
}
