package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoredFileRepo extends JpaRepository<StoredFile, Long> {

    boolean existsBySaltedHash(String saltedHash);

    @Query("SELECT s FROM StoredFile s WHERE s.saltedHash = :saltedHash")
    StoredFile findBySaltedHash(@Param("saltedHash") String saltedHash);
}
