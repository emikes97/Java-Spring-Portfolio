package commerce.eshop.core.tests.application.transactionTest.writer;

import commerce.eshop.core.application.transaction.writer.TransactionWriter;
import commerce.eshop.core.model.entity.Transaction;
import commerce.eshop.core.repository.TransactionRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionWriterTest {

    // == Fields ==
    private TransactionRepo transactionRepo;
    private TransactionWriter transactionWriter;

    // == Tests ==

    @BeforeEach
    void setUp() {
        transactionRepo = mock(TransactionRepo.class);

        transactionWriter = new TransactionWriter(transactionRepo);
    }


    @Test
    void save_success() {
        Transaction transaction = mock(Transaction.class);
        Transaction savedTx = mock(Transaction.class);

        when(transactionRepo.saveAndFlush(transaction)).thenReturn(savedTx);

        Transaction result = transactionWriter.save(transaction);

        assertSame(savedTx, result);
        verify(transactionRepo, times(1)).saveAndFlush(transaction);
    }

    @Test
    void getByIdemKey_found_success() {
        String idemKey = "ABC-123";
        Transaction transaction = mock(Transaction.class);

        when(transactionRepo.findByIdempotencyKeyForUpdate(idemKey))
                .thenReturn(Optional.of(transaction));

        Transaction result = transactionWriter.getByIdemKey(idemKey);

        assertSame(transaction, result);
        verify(transactionRepo, times(1)).findByIdempotencyKeyForUpdate(idemKey);
    }

    @Test
    void getByIdemKey_notFound_throw() {
        String idemKey = "XYZ";

        when(transactionRepo.findByIdempotencyKeyForUpdate(idemKey))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> transactionWriter.getByIdemKey(idemKey)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("Idempotency race lost", ex.getReason());
        verify(transactionRepo, times(1)).findByIdempotencyKeyForUpdate(idemKey);
    }
}