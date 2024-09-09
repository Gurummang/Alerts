package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.AlertSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertSettingsRepo extends JpaRepository<AlertSettings, Long> {

    @Query("SELECT as FROM AlertSettings as WHERE as.adminUsers.id = :adminId")
    List<AlertSettings> findAllByAdminId(@Param("adminId") long adminId);

    @Query("SELECT as FROM AlertSettings as " +
        "JOIN as.adminUsers au " +
        "WHERE au.org.id = :orgId AND as.vt = true")
    List<AlertSettings> findAllByOrgIdAndVtTrue(@Param("orgId") long orgId);
}
