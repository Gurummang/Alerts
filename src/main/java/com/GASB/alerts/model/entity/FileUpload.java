package com.GASB.alerts.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "file_upload")
public class FileUpload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "org_saas_id", nullable = false, referencedColumnName = "id")
    private OrgSaaS orgSaaS;

    @Column(name = "saas_file_id", nullable = false, unique = true)
    private String saasFileId;

    @Column(nullable = false, name="salted_hash")
    private String hash;

    @Column(name = "upload_ts", nullable = false)
    private LocalDateTime timestamp;

    @Builder.Default
    @Column(name = "deleted")
    private boolean deleted = false;

    @ManyToOne
    @JoinColumn(name = "salted_hash", referencedColumnName = "salted_hash", insertable = false, updatable = false)
    private StoredFile storedFile;

    @JsonIgnore
    @OneToOne(mappedBy = "fileUpload", cascade = CascadeType.ALL)
    private TypeScan typeScan;
}
