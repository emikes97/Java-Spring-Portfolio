package commerce.eshop.core.application.transaction.writer;

import commerce.eshop.core.model.entity.Transaction;
import commerce.eshop.core.repository.TransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class TransactionWriter {

    // == Fields ==
    private final TransactionRepo tRepo;

    // == Constructors ==
    @Autowired
    public TransactionWriter(TransactionRepo tRepo) {
        this.tRepo = tRepo;
    }

    // == Public Methods ==
    public Transaction save(Transaction transaction){
        transaction = tRepo.saveAndFlush(transaction);
        return transaction;
    }

    public Transaction getByIdemKey(String idemKey){
        return tRepo.findByIdempotencyKeyForUpdate(idemKey).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency race lost")
        );
    }
}
