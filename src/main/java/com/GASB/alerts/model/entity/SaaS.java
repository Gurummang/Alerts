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
@Table(name = "saas")
public class SaaS {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "saas_name", nullable = false, length = 100)
    private String saasName;

    @OneToMany(mappedBy = "saas", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrgSaaS> orgSaaSList;
}
