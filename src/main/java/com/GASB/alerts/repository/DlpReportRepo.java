package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.DlpReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DlpReportRepo extends JpaRepository<DlpReport, Long> {
}
