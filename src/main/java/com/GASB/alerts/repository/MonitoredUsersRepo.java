package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.MonitoredUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonitoredUsersRepo extends JpaRepository<MonitoredUsers,Long> {
}
