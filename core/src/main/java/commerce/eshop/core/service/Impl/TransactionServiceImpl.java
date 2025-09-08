package commerce.eshop.core.service.Impl;

import commerce.eshop.core.events.PaymentExecutionRequestEvent;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.model.entity.Transaction;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditMessage;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.util.enums.OrderStatus;
import commerce.eshop.core.util.enums.TransactionStatus;
import commerce.eshop.core.repository.CustomerRepo;
import commerce.eshop.core.repository.OrderRepo;
import commerce.eshop.core.repository.TransactionRepo;
import commerce.eshop.core.service.AuditingService;
import commerce.eshop.core.service.TransactionsService;
import commerce.eshop.core.web.dto.requests.Transactions.DTOTransactionRequest;
import commerce.eshop.core.web.dto.requests.Transactions.PaymentVariants.UseNewCard;
import commerce.eshop.core.web.dto.requests.Transactions.PaymentVariants.UseSavedMethod;
import commerce.eshop.core.web.dto.response.Transactions.DTOTransactionResponse;
import commerce.eshop.core.web.mapper.TransactionServiceMapper;
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

    // == Fields ==
    private final CustomerRepo customerRepo;
    private final OrderRepo orderRepo;
    private final TransactionRepo transactionRepo;
    private final ApplicationEventPublisher publisher;
    private final AuditingService auditingService;
    private final TransactionServiceMapper transactionServiceMapper;

    // == Constructors ==
    @Autowired
    public TransactionServiceImpl(CustomerRepo customerRepo, OrderRepo orderRepo, TransactionRepo transactionRepo,
                                  ApplicationEventPublisher publisher, AuditingService auditingService,
                                  TransactionServiceMapper transactionServiceMapper){
        this.customerRepo = customerRepo;
        this.orderRepo = orderRepo;
        this.transactionRepo = transactionRepo;
        this.publisher = publisher;
        this.auditingService = auditingService;
        this.transactionServiceMapper = transactionServiceMapper;
    }

    // == Public Methods ==
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
        if (order.getOrderStatus() != OrderStatus.PENDING_PAYMENT) {
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

        Map<String, Object> snapshot = transactionServiceMapper.toSnapShot(dto.instruction());

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
            return transactionServiceMapper.toDto(transaction);

        } else if (dto.instruction() instanceof UseSavedMethod saved) {
            String paymentMethod = "USE_SAVED_METHOD";
            publishEvent(transaction, customerId, paymentMethod, snapshot);
            auditingService.log(customerId, EndpointsNameMethods.TRANSACTION_PAY, AuditingStatus.SUCCESSFUL, "Payment by -> USE_SAVED_METHOD");
            return transactionServiceMapper.toDto(transaction);
        }

        auditingService.log(customerId, EndpointsNameMethods.TRANSACTION_PAY, AuditingStatus.WARNING, "Unsupported payment instruction");
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment instruction");
    }

    // == Private Methods ==
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

        return transactionServiceMapper.toDto(winner);
    }

    private boolean isTerminal(TransactionStatus s) {
        return s == TransactionStatus.SUCCESSFUL || s == TransactionStatus.FAILED;
    }
}
