package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.OrgSaaS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrgSaaSRepo extends JpaRepository<OrgSaaS, Long> {
}
