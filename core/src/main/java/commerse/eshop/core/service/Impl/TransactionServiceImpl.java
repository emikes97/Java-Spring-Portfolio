package commerse.eshop.core.service.Impl;

import commerse.eshop.core.model.entity.Customer;
import commerse.eshop.core.model.entity.Order;
import commerse.eshop.core.model.entity.Transaction;
import commerse.eshop.core.model.entity.enums.TransactionStatus;
import commerse.eshop.core.repository.CustomerRepo;
import commerse.eshop.core.repository.OrderRepo;
import commerse.eshop.core.repository.TransactionRepo;
import commerse.eshop.core.service.TransactionsService;
import commerse.eshop.core.web.dto.requests.Transactions.DTOTransactionRequest;
import commerse.eshop.core.web.dto.requests.Transactions.PaymentVariants.PaymentInstruction;
import commerse.eshop.core.web.dto.requests.Transactions.PaymentVariants.UseNewCard;
import commerse.eshop.core.web.dto.requests.Transactions.PaymentVariants.UseSavedMethod;
import commerse.eshop.core.web.dto.response.Transactions.DTOTransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

public class TransactionServiceImpl implements TransactionsService {
    
    private final CustomerRepo customerRepo;
    private final OrderRepo orderRepo;
    private final TransactionRepo transactionRepo;

    @Autowired
    public TransactionServiceImpl(CustomerRepo customerRepo, OrderRepo orderRepo, TransactionRepo transactionRepo){
        this.customerRepo = customerRepo;
        this.orderRepo = orderRepo;
        this.transactionRepo = transactionRepo;
    }

    @Transactional
    @Override
    public DTOTransactionResponse pay(UUID customerId, UUID orderId, String idemKey, DTOTransactionRequest dto) {

        Order order = orderRepo.findByCustomer_CustomerIdAndOrderId(customerId, orderId).orElseThrow(
                () -> new NoSuchElementException("The order doesn't exist")
        );

        Customer customer = customerRepo.getReferenceById(customerId);

        Transaction tx = transactionRepo.findByIdempotencyKey(idemKey).orElse(null);

        if(tx != null){
            order = tx.getOrder();

            if (!Objects.equals(tx.getOrder().getOrderId(), orderId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Idempotency key reused for a different order");
            }

            if (isTerminal(tx.getStatus()))
                return toDto(tx);

            // in-flight
            throw new ResponseStatusException(HttpStatus.ACCEPTED,
                    "Transaction already processing");
        }

        Transaction transaction = new Transaction(order, customerId.toString(), toMap(card), order.getTotalOutstanding(), idemKey);
        transactionRepo.findByIdempotencyKeyForUpdate(idemKey);

        if(dto.instruction() instanceof UseNewCard card){

            return newCardPay();

        } else if (dto.instruction() instanceof UseSavedMethod saved) {

            return tokenizedPay();
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment instruction");
    }

    @Transactional
    private DTOTransactionResponse newCardPay(){
        return null;
    }

    @Transactional
    private DTOTransactionResponse tokenizedPay(){
        return null;
    }


    private Map<String, Object> toSnapShot(PaymentInstruction paymentInstruction){
        Map<String, Object> m = new LinkedHashMap<>();

        if (paymentInstruction instanceof UseNewCard card){
            m.put("type", "USE_NEW_CARD");
            m.put("brand", card.brand());
            m.put("panMasked", card.panMasked());     // masked only
            m.put("expMonth", card.expMonth());
            m.put("expYear", card.expYear());
            m.put("holderName", card.holderName());
            return m;
        }
        if (paymentInstruction instanceof UseSavedMethod saved) {
            m.put("type", "USE_SAVED_METHOD");
            m.put("customerPaymentMethodId", saved.customerPaymentMethodId());
            return m;
        }

        m.put("type", "UNKNOWN");
        return m;
    }

    private boolean isTerminal(TransactionStatus s) {
        return s == TransactionStatus.SUCCESSFUL || s == TransactionStatus.FAILED;
    }

    private DTOTransactionResponse toDto(Transaction tr){
        return new DTOTransactionResponse();
    }
}
