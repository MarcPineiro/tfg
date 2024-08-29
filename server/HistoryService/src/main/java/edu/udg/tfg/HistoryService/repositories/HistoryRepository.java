package edu.udg.tfg.HistoryService.repositories;

import edu.udg.tfg.HistoryService.entities.HistoryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<HistoryRecord, Long> {
    Page<HistoryRecord> findByUserIdOrderByActionDateDesc(Long userId, Pageable pageable);
    Page<HistoryRecord> findByUserIdOrderByActionDateAsc(Long userId, Pageable pageable);
}