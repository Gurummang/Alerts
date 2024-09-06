package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoredFileRepo extends JpaRepository<StoredFile, Long> {
}
