package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.AlertSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertSettingsRepo extends JpaRepository<AlertSettings, Long> {

}
