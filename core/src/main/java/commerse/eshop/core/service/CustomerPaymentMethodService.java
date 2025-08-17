package commerse.eshop.core.service;

import commerse.eshop.core.model.entity.CustomerPaymentMethod;
import commerse.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOAddPaymentMethod;
import commerse.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOUpdatePaymentMethod;
import commerse.eshop.core.web.dto.response.PaymentMethod.DTOPaymentMethodResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerPaymentMethodService {

    // == Return all Payment methods of a customer ==
    Page<DTOPaymentMethodResponse> getAllPaymentMethods(UUID customerId, Pageable pageable);

    // == Add New Payment Method
    DTOPaymentMethodResponse addPaymentMethod(UUID customerId, DTOAddPaymentMethod dto);

    // == Update Payment Method
    DTOPaymentMethodResponse updatePaymentMethod(UUID customerId, DTOUpdatePaymentMethod dto);

    // == Retrieve Payment method by ID
    CustomerPaymentMethod retrievePaymentMethod(UUID customerId, UUID paymentMethodId);

    // == Remove Payment Method
    void deletePaymentMethod(UUID customerId, UUID paymentId);
}
