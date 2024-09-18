package com.GASB.alerts.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "dlp_report")
public class DlpReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="policy_id")
    private int policyId;

    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false, referencedColumnName = "id")
    private StoredFile storedFile;

    @Column(name="pii_id")
    private int piiId;

    @Column(name = "info_cnt")
    private int infoCnt;
}