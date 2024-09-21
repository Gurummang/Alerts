package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepo extends JpaRepository<Policy, Long> {

    @Query("SELECT p.policyName FROM Policy p WHERE p.id= :id")
    String findPolicyNameById(@Param("id")long id);
}
