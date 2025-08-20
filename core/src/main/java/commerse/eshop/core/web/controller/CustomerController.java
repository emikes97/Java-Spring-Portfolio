package commerse.eshop.core.web.controller;

import commerse.eshop.core.model.entity.Customer;
import commerse.eshop.core.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService){
        this.customerService = customerService;
    }

    // Get the profile data
    /// curl "http://localhost:8080/api/v1/customers/00000000-0000-0000-0000-000000000001"
    @GetMapping("/{customerId}")
    public Customer getProfile(@PathVariable UUID customerId){
        return customerService.getProfile(customerId);
    }


}
