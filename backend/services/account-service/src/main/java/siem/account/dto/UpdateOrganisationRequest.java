package siem.account.dto;

import lombok.Data;

@Data
public class UpdateOrganisationRequest {
    private String name;
    private String councilIctPhone;
    private String councilPortalUrl;
    private String councilIspProvider;
}
