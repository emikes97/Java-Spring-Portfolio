package commerse.eshop.core.web.controller;

import commerse.eshop.core.model.entity.Customer;
import commerse.eshop.core.service.CustomerService;
import commerse.eshop.core.web.dto.requests.Customer.DTOCustomerUpdateName;
import commerse.eshop.core.web.dto.requests.Customer.DTOCustomerUpdateSurname;
import commerse.eshop.core.web.dto.response.Customer.DTOCustomerCartItemResponse;
import commerse.eshop.core.web.dto.response.Customer.DTOCustomerOrderResponse;
import commerse.eshop.core.web.dto.response.Customer.DTOCustomerResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public DTOCustomerResponse getProfile(@PathVariable UUID customerId){
        return customerService.getProfile(customerId);
    }

    // Get the profile data by searching via phone or email
    /// curl "http://localhost:8080/api/v1/customers/search?phoneOrEmail=%2B306987654321" or
    /// curl "http://localhost:8080/api/v1/customers/search?phoneOrEmail=john.doe@example.com"
    @GetMapping("/search")
    public DTOCustomerResponse getProfile(@RequestParam String phoneOrEmail){
        return customerService.getProfile(phoneOrEmail);
    }

    // Get the customer order with pagination via customer UUID
    ///curl "http://localhost:8080/api/v1/customers/00000000-0000-0000-0000-000000000101/orders?page=0&size=5&sort=completedAt,desc"
    @GetMapping("/{customerId}/orders")
    public Page<DTOCustomerOrderResponse> getOrders(@PathVariable UUID customerId, Pageable pageable){
        return customerService.getOrders(customerId, pageable);
    }

    // Get the customer Cart_Items with pagination via customer UUID
    ///curl "http://localhost:8080/api/v1/customers/00000000-0000-0000-0000-000000000101/cart/items" or
    ///curl "http://localhost:8080/api/v1/customers/00000000-0000-0000-0000-000000000101/cart/items?page=0&size=5&sort=addedAt,desc"
    @GetMapping("/{customerId}/cart/items")
    public Page<DTOCustomerCartItemResponse> getCartItems(@PathVariable UUID customerId,
                                                          @PageableDefault(size = 10, sort = "addedAt", direction = Sort.Direction.DESC) Pageable pageable){
        return customerService.getCartItems(customerId, pageable);
    }

    // Update Customer name
    ///curl -i -X PUT "http://localhost:8080/api/v1/customers/00000000-0000-0000-0000-000000000101/account/name" ^
    /// -H "Content-Type: application/json" ^
    /// -d "{\"password\":\"TempPass123!\",\"name\":\"Michael Pap\"}"
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{customerId}/account/name")
    public void updateName(@PathVariable UUID customerId, @RequestBody @Valid DTOCustomerUpdateName request){
        customerService.updateName(customerId, request.password(), request.name() );
    }

    // Update surname
    /// curl -i -X PUT "http://localhost:8080/api/v1/customers/00000000-0000-0000-0000-000000000101/account/surname" ^
    /// -H "Content-Type: application/json" ^
    /// -d "{\"password\":\"TempPass123!\",\"lastName\":\"Papadopoulos\"}"
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{customerId}/account/surname")
    public void updateSurname(@PathVariable UUID customerId, @RequestBody @Valid DTOCustomerUpdateSurname request){
        customerService.updateSurname(customerId, request.password(), request.lastName());
    }
}
