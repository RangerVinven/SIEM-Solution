package siem.account.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @NotBlank(message = "Password is required")
    @Length(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    // @NotBlank(message = "Role is required")
    // private String role;

    @NotBlank(message = "Organisation ID is required")
    private String organisationId;
}
