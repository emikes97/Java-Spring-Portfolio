package commerse.eshop.core.service.Impl;

import commerse.eshop.core.events.PaymentExecutionRequestEvent;
import commerse.eshop.core.model.entity.Customer;
import commerse.eshop.core.model.entity.Order;
import commerse.eshop.core.model.entity.Transaction;
import commerse.eshop.core.model.entity.consts.EndpointsNameMethods;
import commerse.eshop.core.model.entity.enums.AuditMessage;
import commerse.eshop.core.model.entity.enums.AuditingStatus;
import commerse.eshop.core.model.entity.enums.OrderStatus;
import commerse.eshop.core.model.entity.enums.TransactionStatus;
import commerse.eshop.core.repository.CustomerRepo;
import commerse.eshop.core.repository.OrderRepo;
import commerse.eshop.core.repository.TransactionRepo;
import commerse.eshop.core.service.AuditingService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class TransactionServiceImpl implements TransactionsService {
    
    private final CustomerRepo customerRepo;
    private final OrderRepo orderRepo;
    private final TransactionRepo transactionRepo;
    private final ApplicationEventPublisher publisher;
    private final AuditingService auditingService;

    @Autowired
    public TransactionServiceImpl(CustomerRepo customerRepo, OrderRepo orderRepo, TransactionRepo transactionRepo,
                                  ApplicationEventPublisher publisher, AuditingService auditingService){
        this.customerRepo = customerRepo;
        this.orderRepo = orderRepo;
        this.transactionRepo = transactionRepo;
        this.publisher = publisher;
        this.auditingService = auditingService;
    }

    @Transactional
    @Override
    public DTOTransactionResponse pay(UUID customerId, UUID orderId, String idemKey, DTOTransactionRequest dto) {

        // Input validation
        if (idemKey == null || idemKey.isBlank()) {
            ResponseStatusException bad = new ResponseStatusException(HttpStatus.BAD_REQUEST, "MISSING_IDEM_KEY");
            auditingService.log(customerId, EndpointsNameMethods.TRANSACTION_PAY, AuditingStatus.WARNING, "MISSING_IDEM_KEY");
            throw bad;
        }
        if (dto == null || dto.instruction() == null) {
            ResponseStatusException bad = new ResponseStatusException(HttpStatus.BAD_REQUEST, "MISSING_INSTRUCTION");
            auditingService.log(customerId, EndpointsNameMethods.TRANSACTION_PAY, AuditingStatus.WARNING, "MISSING_INSTRUCTION");
            throw bad;
        }

        final Order order;

        try{
            order = orderRepo.findByCustomer_CustomerIdAndOrderId(customerId, orderId).orElseThrow(
                    () -> new NoSuchElementException("The order doesn't exist")
            );
        } catch (NoSuchElementException e){
            auditingService.log(customerId, EndpointsNameMethods.TRANSACTION_PAY, AuditingStatus.WARNING, e.toString());
            throw e;
        }

        // State gate (mirrors cancel rule)
        if (order.getOrderStatus() != OrderStatus.PENDING_PAYMENT
                && order.getOrderStatus() != OrderStatus.PAYMENT_FAILED) {
            IllegalStateException illegal =
                    new IllegalStateException("INVALID_STATE:" + order.getOrderStatus());
            auditingService.log(customerId, EndpointsNameMethods.TRANSACTION_PAY, AuditingStatus.WARNING, illegal.toString());
            throw illegal;
        }

        if (order.getTotalOutstanding() == null || order.getTotalOutstanding().signum() <= 0) {
            ResponseStatusException bad = new ResponseStatusException(HttpStatus.CONFLICT, "ZERO_OR_NEGATIVE_TOTAL");
            auditingService.log(customerId, EndpointsNameMethods.TRANSACTION_PAY, AuditingStatus.WARNING, "ZERO_OR_NEGATIVE_TOTAL");
            throw bad;
        }

        final Customer customer;

        try{
            customer = customerRepo.findById(customerId).orElseThrow(
                    () -> new NoSuchElementException("The customer with customer_id= " + customerId + " doesn't exist.")
            );
        } catch (NoSuchElementException e){
            auditingService.log(customerId, EndpointsNameMethods.TRANSACTION_PAY, AuditingStatus.WARNING, e.toString());
            throw e;
        }

        Map<String, Object> snapshot = toSnapShot(dto.instruction());

        Transaction transaction = new Transaction(order, customerId.toString(), snapshot, order.getTotalOutstanding(), idemKey);

        try {
            transactionRepo.saveAndFlush(transaction);
            auditingService.log(customerId, EndpointsNameMethods.TRANSACTION_PAY, AuditingStatus.SUCCESSFUL,
                    AuditMessage.TRANSACTION_PAY_SUCCESS.getMessage());
        } catch (DataIntegrityViolationException duplicate){
            Transaction winner = transactionRepo.findByIdempotencyKeyForUpdate(idemKey).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency race lost")
            );
            auditingService.log(customerId, EndpointsNameMethods.TRANSACTION_PAY, AuditingStatus.SUCCESSFUL,
                    "IDEMPOTENT_HIT txn=" + winner.getTransactionId() + " key=" + winner.getIdempotencyKey());
            return resolveWinner(winner, orderId);
        }

        if(dto.instruction() instanceof UseNewCard card){
            String paymentMethod = "USE_NEW_CARD";
            publishEvent(transaction, customerId, paymentMethod, snapshot);
            auditingService.log(customerId, EndpointsNameMethods.TRANSACTION_PAY, AuditingStatus.SUCCESSFUL, "Payment by -> USE_NEW_CARD");
            return toDto(transaction);

        } else if (dto.instruction() instanceof UseSavedMethod saved) {
            String paymentMethod = "USE_SAVED_METHOD";
            publishEvent(transaction, customerId, paymentMethod, snapshot);
            auditingService.log(customerId, EndpointsNameMethods.TRANSACTION_PAY, AuditingStatus.SUCCESSFUL, "Payment by -> USE_SAVED_METHOD");
            return toDto(transaction);
        }

        auditingService.log(customerId, EndpointsNameMethods.TRANSACTION_PAY, AuditingStatus.WARNING, "Unsupported payment instruction");
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
        auditingService.log(customerId, EndpointsNameMethods.EVENT_PUBLISHED, AuditingStatus.SUCCESSFUL, "Transaction_Event_Published");
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
