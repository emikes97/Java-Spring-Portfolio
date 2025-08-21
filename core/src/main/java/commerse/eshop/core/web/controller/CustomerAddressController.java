package commerse.eshop.core.web.controller;

import commerse.eshop.core.service.CustomerAddressService;
import commerse.eshop.core.web.dto.requests.CustomerAddr.DTOAddCustomerAddress;
import commerse.eshop.core.web.dto.requests.CustomerAddr.DTOUpdateCustomerAddress;
import commerse.eshop.core.web.dto.response.CustomerAddr.DTOCustomerAddressResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/addresses")
public class CustomerAddressController {

    private final CustomerAddressService customerAddressService;

    public CustomerAddressController(CustomerAddressService customerAddressService){
        this.customerAddressService = customerAddressService;
    }

    // Get all Addresses
    ///curl "http://localhost:8080/api/v1/customers/00000000-0000-0000-0000-000000000101/addresses?page=0&size=5&sort=addrId,asc"
    @GetMapping
    public Page<DTOCustomerAddressResponse> getAllAddresses(@PathVariable UUID customerId, Pageable pageable){
        return customerAddressService.getAllAddresses(customerId, pageable);
    }

    // Add a new customer Address
    ///curl -i -X POST "http://localhost:8080/api/v1/customers/00000000-0000-0000-0000-000000000101/addresses" ^
    ///   -H "Content-Type: application/json" ^
    ///   -d "{\"country\":\"Greece\",\"street\":\"10 Ermou St\",\"city\":\"Athens\",\"postalCode\":\"10563\",\"isDefault\":false}"
    @PostMapping
    public ResponseEntity<DTOCustomerAddressResponse> addCustomerAddress(@PathVariable UUID customerId, @RequestBody @Valid DTOAddCustomerAddress dto){
        DTOCustomerAddressResponse created = customerAddressService.addCustomerAddress(customerId, dto);
        URI location = URI.create("/api/v1/customers/" + customerId + "/addresses/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    // Update an address
    ///curl -i -X PUT "http://localhost:8080/api/v1/customers/00000000-0000-0000-0000-000000000101/addresses/4" ^
    ///-H "Content-Type: application/json" ^
    ///-d "{\"country\":\"Greece\",\"street\":\"10 Ermou Updated\",\"city\":\"Athens\",\"postalCode\":\"10563\",\"isDefault\":false}"
    @PutMapping("/{id}")
    public DTOCustomerAddressResponse updateCustomerAddress(@PathVariable UUID customerId, @PathVariable long id, @RequestBody @Valid DTOUpdateCustomerAddress dto){
        return customerAddressService.updateCustomerAddress(customerId, id, dto);
    }

    // Make default address
    ///curl -i -X PUT "http://localhost:8080/api/v1/customers/00000000-0000-0000-0000-000000000101/addresses/2/default" ^
    ///   -H "Content-Type: application/json"
    @PutMapping("/{id}/default")
    public DTOCustomerAddressResponse makeDefaultCustomerAddress(@PathVariable UUID customerId, @PathVariable long id){
        return customerAddressService.makeDefaultCustomerAddress(customerId, id);
    }

    // Delete address
    ///curl -i -X DELETE "http://localhost:8080/api/v1/customers/00000000-0000-0000-0000-000000000101/addresses/4" ^
    ///   -H "Content-Type: application/json"
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteCustomerAddress(@PathVariable UUID customerId, @PathVariable long id){
        customerAddressService.deleteCustomerAddress(customerId, id);
    }
}
