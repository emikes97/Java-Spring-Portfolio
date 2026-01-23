// package commerce.eshop.core.application.async.internal;

// import commerce.eshop.core.application.email.EmailComposer;
// import commerce.eshop.core.application.events.email.EmailEventRequest;
// import commerce.eshop.core.application.events.payments.PaymentExecutionRequestEvent;
// import commerce.eshop.core.application.events.payments.PaymentSucceededOrFailed;
// import commerce.eshop.core.model.entity.Customer;
// import commerce.eshop.core.model.entity.Order;
// import commerce.eshop.core.model.entity.Transaction;
// import commerce.eshop.core.application.infrastructure.DomainLookupService;
// import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
// import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
// import commerce.eshop.core.application.util.enums.AuditingStatus;
// import commerce.eshop.core.application.util.enums.OrderStatus;
// import commerce.eshop.core.application.util.enums.TransactionStatus;
// import commerce.eshop.core.repository.OrderRepo;
// import commerce.eshop.core.repository.TransactionRepo;
// import commerce.eshop.core.application.async.external.contracts.PaymentProviderClient;
// import commerce.eshop.core.web.dto.response.Providers.Charging.ProviderChargeResult;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.ApplicationEventPublisher;
// import org.springframework.scheduling.annotation.Async;
// import org.springframework.stereotype.Component;
// import org.springframework.transaction.annotation.Propagation;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.transaction.event.TransactionPhase;
// import org.springframework.transaction.event.TransactionalEventListener;

// import java.time.OffsetDateTime;
// import java.util.Map;
// import java.util.Optional;
// import java.util.UUID;

// @Component
// @Slf4j
// public class PaymentProcessor {

//     // == Fields ==

//     private final PaymentProviderClient paymentProviderClient;
//     private final TransactionRepo transactionRepo;
//     private final OrderRepo orderRepo;
//     private final ApplicationEventPublisher publisher;
//     private final CentralAudit centralAudit;
//     private final EmailComposer emailComposer;
//     private final DomainLookupService domainLookupService;

//     // == Constructors ==

//     @Autowired
//     public PaymentProcessor(PaymentProviderClient paymentProviderClient, TransactionRepo transactionRepo, OrderRepo orderRepo,
//                             ApplicationEventPublisher publisher, CentralAudit centralAudit, EmailComposer emailComposer,
//                             DomainLookupService domainLookupService){

//         this.paymentProviderClient = paymentProviderClient;
//         this.transactionRepo = transactionRepo;
//         this.orderRepo = orderRepo;
//         this.publisher = publisher;
//         this.centralAudit = centralAudit;
//         this.emailComposer = emailComposer;
//         this.domainLookupService = domainLookupService;
//     }

//     // == Public Methods ==
//     @Async("asyncExecutor")
//     @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//     @Transactional(propagation = Propagation.REQUIRES_NEW)
//     public void processPayment(PaymentExecutionRequestEvent paymentExecutionRequestEvent){

//         final Transaction tr;

//         try {
//             tr = transactionRepo.findByIdempotencyKeyForUpdate(paymentExecutionRequestEvent.idemKey())
//                     .orElseThrow(() -> new IllegalArgumentException("No transaction with idemKey = " + paymentExecutionRequestEvent.idemKey()));
//         } catch (IllegalArgumentException ex){
//             throw centralAudit.audit(ex, paymentExecutionRequestEvent.customerId(), EndpointsNameMethods.PAYMENT_PROCESSING_ASYNC, AuditingStatus.WARNING, ex.toString());
//         }


//         if(!paymentExecutionRequestEvent.orderId().equals(tr.getOrder().getOrderId())){
//             centralAudit.warn(paymentExecutionRequestEvent.customerId(), EndpointsNameMethods.PAYMENT_PROCESSING_ASYNC, AuditingStatus.ERROR,
//                     "Transaction IDs mismatch");
//             return;
//         }
//         if (tr.getStatus() == TransactionStatus.SUCCESSFUL || tr.getStatus() == TransactionStatus.FAILED){
//             centralAudit.info(paymentExecutionRequestEvent.customerId(), EndpointsNameMethods.PAYMENT_PROCESSING_ASYNC, AuditingStatus.SUCCESSFUL,
//                     "Transaction Status Terminal");
//             return;
//         }

//         Map<String, Object> snap = Optional.ofNullable(paymentExecutionRequestEvent.snapshot()).orElse(Map.of());
//         ProviderChargeResult providerChargeResult = null;

//         try{
//             switch (paymentExecutionRequestEvent.methodType()){
//                 case "USE_SAVED_METHOD" -> {
//                     Object idObjPaymentMethod = snap.get("customerPaymentMethodId");
//                     UUID customerPaymentMethodId = (idObjPaymentMethod instanceof UUID u) ? u : UUID.fromString(String.valueOf(idObjPaymentMethod));
//                     providerChargeResult = paymentProviderClient.chargeWithSavedMethod(tr, customerPaymentMethodId);
//                 }
//                 case "USE_NEW_CARD" -> {
//                     String brand = (String) snap.get("brand");
//                     String panMasked = (String) snap.get("panMasked");
//                     Integer expMonth = toInt(snap.get("expMonth"));
//                     Integer expYear  = toInt(snap.get("expYear"));
//                     String holder    = (String) snap.get("holderName");
//                     providerChargeResult = paymentProviderClient.tokenizeAndCharge(tr, brand, panMasked, expMonth, expYear, holder);
//                 }

//                 default -> {
//                     throw centralAudit.audit(new IllegalArgumentException("Unsupported payment method: " + paymentExecutionRequestEvent.methodType()),
//                             paymentExecutionRequestEvent.customerId(), EndpointsNameMethods.PAYMENT_PROCESSING_ASYNC, AuditingStatus.WARNING,
//                             "Unsupported payment method");
//                 }
//             }
//         } catch (Exception ex) {
//             centralAudit.info(paymentExecutionRequestEvent.customerId(), EndpointsNameMethods.PAYMENT_PROCESSING_ASYNC, AuditingStatus.ERROR,
//                     ex.toString());
//             showErrorMessage(providerChargeResult, ex, tr);
//             return;
//         }

//         if(providerChargeResult.successful()){
//             tr.setStatus(TransactionStatus.SUCCESSFUL);
//             tr.setCompletedAt(OffsetDateTime.now());
//             tr.setProviderReference(providerChargeResult.providerReference());
//             log.info("Transaction: " + tr.getTransactionId() + "was completed");
//             transactionRepo.save(tr);
//             publisher.publishEvent(new PaymentSucceededOrFailed(OrderStatus.PAID, OffsetDateTime.now(), tr.getOrder().getOrderId()));
//             publishPaymentEmail(paymentExecutionRequestEvent, tr, true);
//             centralAudit.info(paymentExecutionRequestEvent.customerId(), EndpointsNameMethods.PAYMENT_PROCESSING_ASYNC, AuditingStatus.SUCCESSFUL,
//                     "Transaction Succeeded");
//         } else {
//             tr.setStatus(TransactionStatus.FAILED);
//             tr.setCompletedAt(OffsetDateTime.now());
//             tr.setProviderReference(providerChargeResult.providerReference());
//             log.info("Transaction: " + tr.getTransactionId() + "failed");
//             transactionRepo.save(tr);
//             centralAudit.info(paymentExecutionRequestEvent.customerId(), EndpointsNameMethods.PAYMENT_PROCESSING_ASYNC, AuditingStatus.FAILED,
//                     "Transaction Failed");
//             publisher.publishEvent(new PaymentSucceededOrFailed(OrderStatus.PAYMENT_FAILED, OffsetDateTime.now(), tr.getOrder().getOrderId()));
//             publishPaymentEmail(paymentExecutionRequestEvent, tr, false);
//         }
//     }


//     // == Private Methods ==

//     private void showErrorMessage(ProviderChargeResult providerChargeResult, Exception exception, Transaction tr){
//         if (providerChargeResult == null) {
//             // provider wasn't called or failed before returning any result
//             providerChargeResult = new ProviderChargeResult(null, false, "PROVIDER_ERROR", exception.getMessage());
//         }
//         log.debug("[ERROR]: " + providerChargeResult.errorCode() + " " + providerChargeResult.errorMessage() + " -- " + exception);
//         log.info("[ERROR]: " + providerChargeResult.errorCode() + " for transaction " + tr.getTransactionId());
//         tr.setStatus(TransactionStatus.FAILED);
//         tr.setCompletedAt(OffsetDateTime.now());
//         transactionRepo.save(tr);
//         publisher.publishEvent(new PaymentSucceededOrFailed(OrderStatus.PAYMENT_FAILED, OffsetDateTime.now(), tr.getOrder().getOrderId()));
//     }

//     private Integer toInt(Object o){
//         if (o == null) return null;
//         if (o instanceof Integer i) return i;
//         if (o instanceof Number n) return n.intValue();
//         return Integer.parseInt(o.toString());
//     }

//     private void publishPaymentEmail(PaymentExecutionRequestEvent pm, Transaction tr, Boolean successful){
//         Customer customer = domainLookupService.getCustomerOrThrow(pm.customerId(), EndpointsNameMethods.GET_PROFILE_BY_ID);
//         Order order = domainLookupService.getOrderOrThrow(customer.getCustomerId(), pm.orderId(), EndpointsNameMethods.ORDER_VIEW);
//         EmailEventRequest event = null;
//         if(successful){
//             event = emailComposer.paymentConfirmed(customer, order, tr, tr.getTotalOutstanding(), "Euro");
//         } else {
//             event = emailComposer.paymentFailed(customer, order, tr, "Transaction Failed");
//         }
//         publisher.publishEvent(event);
//     }
// }
