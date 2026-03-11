package siem.account.dto;

import lombok.Data;

@Data
public class AddEmployeeRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;
}
