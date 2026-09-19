package se.jennifer.customerservice.security;

import se.jennifer.customerservice.model.Customer;
import se.jennifer.customerservice.repository.CustomerRepo;
import se.jennifer.customerservice.service.CustomerService;
import se.jennifer.customerservice.dto.CreateCustomerRequest;
import se.jennifer.customerservice.dto.CustomerResponse;
import se.jennifer.customerservice.dto.LoginRequest;
import se.jennifer.customerservice.error.BadRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
public class LoginController {

    private final CustomerRepo customerRepo;
    private final PasswordEncoder passwordEncoder;
    private final CustomerService customerService;

    public LoginController(CustomerRepo customerRepo, PasswordEncoder passwordEncoder, CustomerService customerService) {
        this.customerRepo = customerRepo;
        this.passwordEncoder = passwordEncoder;
        this.customerService = customerService;
    }

    @PostMapping("/login")
    public CustomerResponse login(@RequestBody LoginRequest request) {
        Customer customer = customerRepo
                .findByEmail(request.email())
                .orElseThrow(() -> new BadRequest("Invalid email or password"));

        boolean passwordMatches = passwordEncoder.matches(request.password(), customer.getPasswordHash());

        if (!passwordMatches) {
            throw new BadRequest("Invalid email or password");
        }
        return new CustomerResponse(customer.getId(),customer.getFirstName(),
                customer.getLastName(), customer.getEmail(), customer.getPhoneNumber());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse registerCustomer(@RequestBody CreateCustomerRequest request) {
        return customerService.createCustomer(request);
    }
}
