package se.jennifer.customerservice.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import se.jennifer.customerservice.dto.CreateCustomerRequest;
import se.jennifer.customerservice.dto.CustomerResponse;
import se.jennifer.customerservice.dto.UpdateCustomerRequest;
import se.jennifer.customerservice.error.BadRequest;
import se.jennifer.customerservice.error.ConflictException;
import se.jennifer.customerservice.error.NotFoundException;
import se.jennifer.customerservice.error.ServiceUnavailableException;
import se.jennifer.customerservice.model.Customer;
import se.jennifer.customerservice.repository.CustomerRepo;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepo customerRepo;
    private final BookingClient bookingClient;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepo customerRepo, BookingClient bookingClient, PasswordEncoder passwordEncoder) {
        this.customerRepo = customerRepo;
        this.bookingClient = bookingClient;
        this.passwordEncoder = passwordEncoder;
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepo.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer with id " + id + " not found"));

        return toDTO(customer);
    }

    public CustomerResponse createCustomer(CreateCustomerRequest request) {

        if (customerRepo.existsByEmail(request.email())) {
            throw new BadRequest("Customer with email " + request.email() + " already exists");
        }

        Customer customer = new Customer();
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setEmail(request.email());
        customer.setPhoneNumber(request.phoneNumber());
        customer.setPasswordHash(passwordEncoder.encode(request.password()));

        try {
            Customer saved = customerRepo.save(customer);
            return toDTO(saved);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequest("Customer with email " + request.email() + " already exists");
        }
    }

    public void deleteCustomer(Long id) {

        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer with id " + id + " not found"));

        Boolean hasBookings;

        try {
            hasBookings = bookingClient.hasActiveBookings(id);
        } catch (ServiceUnavailableException e) {
            // booking-service är verkligen nere
            throw e;
        } catch (Exception e) {
            // något oväntat fel i bookingClient → logga men kasta inte 503
            throw new RuntimeException("Unexpected error while checking bookings", e);
        }

        if (hasBookings) {
            throw new ConflictException("Customer with id " + id + " has active bookings and cannot be deleted");
        }

        customerRepo.delete(customer);
    }

    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request) {

        Customer customer = customerRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer with id " + id + " not found"));

        if (request.firstName() != null) customer.setFirstName(request.firstName());
        if (request.lastName() != null) customer.setLastName(request.lastName());
        if (request.phoneNumber() != null) customer.setPhoneNumber(request.phoneNumber());

        if (request.email() != null) {
            if (!request.email().equals(customer.getEmail()) &&
                    customerRepo.existsByEmail(request.email())) {
                throw new BadRequest("Customer with email " + request.email() + " already exists");
            }
            customer.setEmail(request.email());
        }

        if (request.password() != null) {
            customer.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        try {
            Customer saved = customerRepo.save(customer);
            return toDTO(saved);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequest("Could not update customer");
        }
    }

    private CustomerResponse toDTO(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhoneNumber()
        );
    }
}
