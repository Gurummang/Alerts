package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.Activities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivitiesRepo extends JpaRepository<Activities, Long> {
}
