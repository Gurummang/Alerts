package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FileUploadRepo extends JpaRepository<FileUpload, Long> {

    @Query("SELECT fu.orgSaaS.org.id FROM FileUpload fu WHERE fu.id = :uploadId")
    Long findOrgIdByUploadId(@Param("uploadId") long uploadId);
}
