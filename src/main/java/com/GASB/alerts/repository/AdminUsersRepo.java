package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.AdminUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminUsersRepo extends JpaRepository<AdminUsers, Long> {
}
