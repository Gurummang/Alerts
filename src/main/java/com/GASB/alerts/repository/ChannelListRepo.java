package com.GASB.alerts.repository;

import com.GASB.alerts.model.entity.ChannelList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelListRepo extends JpaRepository<ChannelList, Long> {
}
