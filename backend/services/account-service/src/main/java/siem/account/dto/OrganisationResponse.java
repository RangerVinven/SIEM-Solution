package siem.account.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class OrganisationResponse {
    private UUID id;
    private String name;
}
