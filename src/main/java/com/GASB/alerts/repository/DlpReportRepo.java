package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.DlpReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DlpReportRepo extends JpaRepository<DlpReport, Long> {
}
