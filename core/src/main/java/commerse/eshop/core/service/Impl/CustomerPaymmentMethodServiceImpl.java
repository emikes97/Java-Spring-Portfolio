package commerse.eshop.core.service.Impl;

import commerse.eshop.core.model.entity.CustomerPaymentMethod;
import commerse.eshop.core.service.CustomerPaymentMethodService;
import commerse.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOAddPaymentMethod;
import commerse.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOUpdatePaymentMethod;
import commerse.eshop.core.web.dto.response.PaymentMethod.DTOPaymentMethodResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public class CustomerPaymmentMethodServiceImpl implements CustomerPaymentMethodService {

    @Override
    public Page<DTOPaymentMethodResponse> getAllPaymentMethods(UUID customerId, Pageable pageable) {
        return null;
    }

    @Override
    public DTOPaymentMethodResponse addPaymentMethod(UUID customerId, DTOAddPaymentMethod dto) {
        return null;
    }

    @Override
    public DTOPaymentMethodResponse updatePaymentMethod(UUID customerId, DTOUpdatePaymentMethod dto) {
        return null;
    }

    @Override
    public CustomerPaymentMethod retrievePaymentMethod(UUID customerId, UUID paymentMethodId) {
        return null;
    }

    @Override
    public void deletePaymentMethod(UUID customerId, UUID paymentId) {

    }

    private DTOPaymentMethodResponse toDo(CustomerPaymentMethod p){
        return new DTOPaymentMethodResponse(
                p.getProvider(),
                p.getBrand(),
                p.getLast4(),
                p.getYearExp(),
                p.getMonthExp(),
                p.isDefault(),
                p.getCreatedAt()
        );}
}
