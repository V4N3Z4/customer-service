package se.jennifer.customerservice.dto;

public record CreateCustomerRequest(
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String password
) {}

