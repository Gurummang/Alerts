package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.Gscan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GscanRepo extends JpaRepository<Gscan, Long> {
}
