package commerce.eshop.core.service.Impl;

import commerce.eshop.core.application.order.commands.CancelOrder;
import commerce.eshop.core.application.order.queries.OrderQueries;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditMessage;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.service.OrderService;
import commerce.eshop.core.web.dto.response.Order.DTOOrderDetailsResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;


import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    // == Fields ==
    private final CancelOrder cancelOrder;
    private final OrderQueries queries;
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public OrderServiceImpl(CancelOrder cancelOrder, OrderQueries queries,
                            CentralAudit centralAudit){

        this.cancelOrder = cancelOrder;
        this.queries = queries;
        this.centralAudit = centralAudit;
    }

    // == Public Methods ==

//    @Override Not used anymore, changed to Async
//    public DTOOrderPlacedResponse placeOrder(UUID customerId, DTOOrderCustomerAddress addressDto) {
//        DTOOrderPlacedResponse orderRes = placeOrder.handle(customerId, addressDto);
//        centralAudit.info(customerId, EndpointsNameMethods.ORDER_PLACE, AuditingStatus.SUCCESSFUL, AuditMessage.ORDER_PLACE_SUCCESS.getMessage());
//        return orderRes;
//    }

    @Override
    public void cancel(UUID customerId, UUID orderId) {
        cancelOrder.handle(customerId, orderId);
        centralAudit.info(customerId, EndpointsNameMethods.ORDER_CANCEL, AuditingStatus.SUCCESSFUL, AuditMessage.ORDER_CANCEL_SUCCESS.getMessage());
    }

    @Override
    public DTOOrderDetailsResponse viewOrder(UUID customerId, UUID orderId) {
        DTOOrderDetailsResponse orderRes = queries.viewOrder(customerId, orderId);
        centralAudit.info(customerId, EndpointsNameMethods.ORDER_VIEW, AuditingStatus.SUCCESSFUL, AuditMessage.ORDER_VIEW_SUCCESS.getMessage());
        return orderRes;
    }
}