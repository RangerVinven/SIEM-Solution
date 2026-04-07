package siem.account.dto;

import lombok.Data;

@Data
public class CreateSchoolRequest {
    private String name;
    private String buildingCode;
    private String siteLocation;
    private String serverRoomLocation;
    private String serverCabinetKeyInfo;
}
