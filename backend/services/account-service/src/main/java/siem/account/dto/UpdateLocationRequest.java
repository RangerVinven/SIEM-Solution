package siem.account.dto;

import lombok.Data;

@Data
public class UpdateLocationRequest {
    private String name;
    private String department;
    private String roomNumber;
}
