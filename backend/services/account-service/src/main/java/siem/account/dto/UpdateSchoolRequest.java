package siem.account.dto;

import lombok.Data;

@Data
public class UpdateSchoolRequest {
    private String name;
    private String buildingCode;
    private String siteLocation;
    private String serverRoomLocation;
    private String serverCabinetKeyInfo;
}
