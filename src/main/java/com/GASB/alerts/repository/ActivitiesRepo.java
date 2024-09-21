package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.Activities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ActivitiesRepo extends JpaRepository<Activities, Long> {

    @Query("SELECT a FROM Activities a WHERE a.saasFileId = :saasFileId AND a.eventTs = :timestamp")
    Activities findAllBySaasFileIdAndTimeStamp(@Param("saasFileId") String saasFileId, @Param("timestamp") LocalDateTime timestamp);
}
