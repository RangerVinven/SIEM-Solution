package siem.account.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class SchoolResponse {
    private UUID id;
    private String name;
    private String buildingCode;
    private String siteLocation;
    private String serverRoomLocation;
    private String serverCabinetKeyInfo;
}
