package commerce.eshop.core.service;

import commerce.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOAddPaymentMethod;
import commerce.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOUpdatePaymentMethod;
import commerce.eshop.core.web.dto.response.PaymentMethod.DTOPaymentMethodResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerPaymentMethodService {

    // == Return all Payment methods of a customer ==
    Page<DTOPaymentMethodResponse> getAllPaymentMethods(UUID customerId, Pageable pageable);

    // == Add New Payment Method
    DTOPaymentMethodResponse addPaymentMethod(UUID customerId, DTOAddPaymentMethod dto);

    // == Update Payment Method
    DTOPaymentMethodResponse updatePaymentMethod(UUID customerId, UUID paymentMethodId, DTOUpdatePaymentMethod dto);

    // == Retrieve Payment method by ID
    DTOPaymentMethodResponse retrievePaymentMethod(UUID customerId, UUID paymentMethodId);

    // == Remove Payment Method
    void deletePaymentMethod(UUID customerId, UUID paymentId);
}
