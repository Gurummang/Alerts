package com.GASB.alerts.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "type_scan")
public class TypeScan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "upload_id", nullable = false, referencedColumnName = "id")
    private FileUpload fileUpload;

    @Column(name = "correct")
    private Boolean correct;

    @Column(name="mimetype")
    private String mimetype;

    @Column(name = "signature")
    private String signature;

    @Column(name = "extension")
    private String extension;
}
