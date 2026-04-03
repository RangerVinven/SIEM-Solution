package siem.account.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "schools")
public class School {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private UUID organisationId;

    private String buildingCode;

    private String siteLocation;

    private String hostnameRegex;

    private String localHelpdeskPhone;

    private String serverRoomLocation;

    private String serverCabinetKeyInfo;
}
