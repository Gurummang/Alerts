package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.VtReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VtReportRepo extends JpaRepository<VtReport, Long> {
}
