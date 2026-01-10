package commerce.eshop.core.application.checkout.commands;

import commerce.eshop.core.web.dto.response.Checkout.DTOCheckoutResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProcessCheckout {

    // == Fields ==

    // == Constructors ==

    // == Public Methods ==

    @Transactional
    public DTOCheckoutResponse process(){
        return null;
    }
}
