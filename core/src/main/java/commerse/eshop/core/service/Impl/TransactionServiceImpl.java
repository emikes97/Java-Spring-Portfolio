package commerse.eshop.core.service.Impl;

import commerse.eshop.core.events.PaymentExecutionRequestEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

public class TransactionServiceImpl implements TransactionsService {
    
    private final CustomerRepo customerRepo;
    private final OrderRepo orderRepo;
    private final TransactionRepo transactionRepo;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public TransactionServiceImpl(CustomerRepo customerRepo, OrderRepo orderRepo, TransactionRepo transactionRepo, ApplicationEventPublisher publisher){
        this.customerRepo = customerRepo;
        this.orderRepo = orderRepo;
        this.transactionRepo = transactionRepo;
        this.publisher = publisher;
    }

    @Transactional
    @Override
    public DTOTransactionResponse pay(UUID customerId, UUID orderId, String idemKey, DTOTransactionRequest dto) {

        Order order = orderRepo.findByCustomer_CustomerIdAndOrderId(customerId, orderId).orElseThrow(
                () -> new NoSuchElementException("The order doesn't exist")
        );

        Customer customer = customerRepo.getReferenceById(customerId);

        Map<String, Object> snapshot = toSnapShot(dto.instruction());

        Transaction transaction = new Transaction(order, customerId.toString(), snapshot, order.getTotalOutstanding(), idemKey);

        try {
            transactionRepo.saveAndFlush(transaction);
        } catch (DataIntegrityViolationException duplicate){
            Transaction winner = transactionRepo.findByIdempotencyKeyForUpdate(idemKey).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency race lost")
            );
            return resolveWinner(winner, orderId);
        }

        if(dto.instruction() instanceof UseNewCard card){
            String paymentMethod = "USE_NEW_CARD";
            publishEvent(transaction, customerId, paymentMethod, snapshot);
            return toDto(transaction);

        } else if (dto.instruction() instanceof UseSavedMethod saved) {
            String paymentMethod = "USE_SAVED_METHOD";
            publishEvent(transaction, customerId, paymentMethod, snapshot);
            return toDto(transaction);
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment instruction");
    }

    private void publishEvent(Transaction transaction, UUID customerId, String paymentMethod, Map<String, Object> snapshot){
        publisher.publishEvent(new PaymentExecutionRequestEvent(transaction.getTransactionId(),
                transaction.getOrder().getOrderId(),
                customerId,
                paymentMethod,
                snapshot,
                transaction.getTotalOutstanding(),
                transaction.getIdempotencyKey()
        ));
    }

    private DTOTransactionResponse resolveWinner(Transaction winner, UUID expectedOrderId){
        if (!Objects.equals(winner.getOrder().getOrderId(), expectedOrderId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Idempotency key reused for a different order");
        }

        if (winner.getStatus() == TransactionStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.ACCEPTED, "Transaction already processing");
        }

        return toDto(winner);
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
        return new DTOTransactionResponse(
                tr.getTransactionId(),
                tr.getOrder().getOrderId(),
                tr.getCustomerId(),
                tr.getPaymentMethod(),
                tr.getTotalOutstanding(),
                tr.getStatus(),
                tr.getSubmittedAt(),
                tr.getCompletedAt()
        );
    }
}
