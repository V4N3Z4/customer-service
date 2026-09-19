package se.jennifer.customerservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import se.jennifer.customerservice.dto.CreateCustomerRequest;
import se.jennifer.customerservice.dto.CustomerResponse;
import se.jennifer.customerservice.dto.UpdateCustomerRequest;
import se.jennifer.customerservice.service.CustomerService;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<CustomerResponse> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{id}")
    public CustomerResponse getCustomer(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(@RequestBody CreateCustomerRequest request) {
        return customerService.createCustomer(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
    }

    @PutMapping("/{id}")
    public CustomerResponse updateCustomer(
            @PathVariable Long id,
            @RequestBody UpdateCustomerRequest request) {
        return customerService.updateCustomer(id, request);
    }
}

