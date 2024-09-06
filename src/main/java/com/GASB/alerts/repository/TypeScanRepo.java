package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.TypeScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeScanRepo extends JpaRepository<TypeScan, Long> {
}
