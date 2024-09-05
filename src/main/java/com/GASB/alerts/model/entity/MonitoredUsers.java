package com.GASB.alerts.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "monitored_users")
public class MonitoredUsers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id", length = 100)
    private String userId;

    @ManyToOne
    @JoinColumn(name = "org_saas_id", nullable = false,referencedColumnName = "id")
    private OrgSaaS orgSaaS;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(name = "status")
    private Timestamp timestamp;
}

