package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.AlertEmails;
import com.GASB.alerts.model.entity.AlertSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AlertEmailsRepo extends JpaRepository<AlertEmails, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM AlertEmails ae WHERE ae.alertSettings = :alertSettings")
    void deleteByAlertSettings(AlertSettings alertSettings);

    @Query("SELECT ae.email FROM AlertEmails ae WHERE ae.alertSettings.id = :alertId")
    List<String> findEmailByAlertId(@Param("alertId") long alertId);
}

