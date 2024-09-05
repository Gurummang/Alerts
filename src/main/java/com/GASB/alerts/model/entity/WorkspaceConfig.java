package com.GASB.alerts.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "workspace_config")
public class WorkspaceConfig {
    @Id
    @Column(name = "id", nullable = false)
    private int id; // This corresponds to id in OrgSaaS

    @Column(name = "workspace_name", nullable = false, length = 100)
    private String workspaceName;

    @Column(name = "saas_admin_email", nullable = false, length = 100)
    private String saasAdminEmail;

    @Column(name = "token", nullable = false, length = 100)
    private String token;

    @Column(name = "webhook")
    private String webhook;

    @Column(name = "alias")
    private String alias;

    @Column(name = "register_date", nullable = false)
    private Timestamp registerDate;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private OrgSaaS orgSaas;
}
