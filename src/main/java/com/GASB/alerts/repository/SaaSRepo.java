package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.SaaS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaaSRepo extends JpaRepository<SaaS, Long> {
}
