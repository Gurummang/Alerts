package com.GASB.alerts.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "dlp_report")
public class DlpReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false)
    private StoredFile storedFile; // DlpReport가 하나의 StoredFile을 참조함

    @ManyToOne
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @ManyToOne
    @JoinColumn(name = "pii_id", nullable = false)
    private Pii pii;

    @Column(name = "info_cnt")
    private int infoCnt;
}