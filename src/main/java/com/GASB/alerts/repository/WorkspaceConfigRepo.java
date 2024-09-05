package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.WorkspaceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceConfigRepo extends JpaRepository<WorkspaceConfig, Long> {
}
