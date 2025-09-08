package commerse.eshop.core.web.mapper;

import commerse.eshop.core.model.entity.CustomerPaymentMethod;
import commerse.eshop.core.web.dto.response.PaymentMethod.DTOPaymentMethodResponse;
import org.springframework.stereotype.Component;

@Component
public class CustomerPaymentMethodServiceMapper {

    public DTOPaymentMethodResponse toDto(CustomerPaymentMethod p){
        return new DTOPaymentMethodResponse(
                p.getProvider(),
                p.getBrand(),
                p.getLast4(),
                p.getYearExp(),
                p.getMonthExp(),
                p.getTokenStatus(),
                p.isDefault(),
                p.getCreatedAt()
        );}
}
