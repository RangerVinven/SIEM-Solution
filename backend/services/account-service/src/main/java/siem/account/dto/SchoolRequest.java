package siem.account.dto;

import lombok.Data;

@Data
public class SchoolRequest {
    private String name;
    private String buildingCode;
    private String siteLocation;
    private String hostnameRegex;
    private String localHelpdeskPhone;
    private String serverRoomLocation;
    private String serverCabinetKeyInfo;
}
