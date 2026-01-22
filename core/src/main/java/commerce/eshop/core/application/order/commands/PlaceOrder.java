//package commerce.eshop.core.application.order.commands;
//
//import commerce.eshop.core.application.order.orchestrator.OrderPlacementExecutor;
//import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
//import commerce.eshop.core.web.dto.response.Order.DTOOrderPlacedResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.dao.CannotSerializeTransactionException;
//import org.springframework.dao.DeadlockLoserDataAccessException;
//import org.springframework.stereotype.Component;
//
//import java.util.UUID;
//import java.util.concurrent.ThreadLocalRandom;
//
//@Component
//public class PlaceOrder {
//
//    // == Fields ==
//    private final OrderPlacementExecutor executor; // Orchestrator
//
//    // == Constructors ==
//    @Autowired
//    public PlaceOrder(OrderPlacementExecutor executor) {
//        this.executor = executor;
//    }
//
//    // == Public Methods ==
//    /**
//     * ENTRYPOINT (non-transactional): bounded retry with small random backoff.
//     */
//    public DTOOrderPlacedResponse handle(UUID customerId, DTOOrderCustomerAddress addressDto) {
//        int attempts = 0;
//        while (true) {
//            try {
//                return executor.tryPlaceOrder(customerId, addressDto);
//            } catch (CannotSerializeTransactionException | DeadlockLoserDataAccessException ex) {
//                if (++attempts >= 5) {
//                    throw ex;
//                }
//                int backoffMs = ThreadLocalRandom.current().nextInt(100, 300);
//                try {
//                    Thread.sleep(backoffMs);
//                } catch (InterruptedException ie) {
//                    Thread.currentThread().interrupt();
//                    throw new IllegalStateException("Interrupted while retrying order placement", ie);
//                }
//            }
//        }
//    }
//}
