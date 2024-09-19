package com.GASB.alerts.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "org")
public class Org {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "org_name", nullable = false, length = 100)
    private String orgName;

    @OneToMany(mappedBy = "org", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrgSaaS> orgSaaSList;
}
