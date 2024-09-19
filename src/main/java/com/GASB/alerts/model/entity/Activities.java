package com.GASB.alerts.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "activities")
public class Activities {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "id", nullable = false)
    private FileGroup fileGroup;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private MonitoredUsers user;

    @Column(name = "event_type", length = 100)
    private String eventType;

    @Column(name = "saas_file_id", length = 64)
    private String saasFileId;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "tlsh", columnDefinition = "TEXT", nullable = false)
    private String tlsh;

    @Column(name = "event_ts")
    private LocalDateTime eventTs;

    @Column(name = "upload_channel", length = 100)
    private String uploadChannel;
}