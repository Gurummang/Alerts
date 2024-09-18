package com.GASB.alerts.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "policy")
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "org_saas_id", referencedColumnName = "id", nullable = false)
    private OrgSaaS orgSaas; // OrgSaaS와의 ManyToOne 관계

    @Column(name = "policy_name")
    private String policyName;

    @Column(name = "description")
    private String description;

    @Column(name = "identify")
    private boolean identify;

    @Column(name = "passport")
    private boolean passport;

    @Column(name = "drive")
    private boolean drive;

    @Column(name = "foreigner")
    private boolean foreigner;

    @Column(name = "comment")
    private String comment;

    @OneToMany(mappedBy = "policy")
    private Set<DlpReport> dlpReports; // DlpReport와의 OneToMany 관계

}
