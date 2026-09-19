package se.jennifer.customerservice.dto;


public record CustomerResponse
        (Long id, String firstName, String lastName, String email, String phoneNumber){
}
