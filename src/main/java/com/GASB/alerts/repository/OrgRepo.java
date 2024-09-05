package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.Org;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrgRepo extends JpaRepository<Org, Long> {
}
