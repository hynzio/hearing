package org.hdmd.hearingdemo.repository;
import org.hdmd.hearingdemo.model.History;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    List<History> findByDeviceId(Long device_id, Sort sort);

}

