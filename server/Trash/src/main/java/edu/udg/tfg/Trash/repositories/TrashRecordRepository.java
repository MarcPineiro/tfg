package edu.udg.tfg.Trash.repositories;

import edu.udg.tfg.Trash.entities.TrashRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrashRecordRepository extends JpaRepository<TrashRecord, UUID> {
    List<TrashRecord> findByUserId(UUID userId);
    Optional<TrashRecord> findByElementIdAndUserId(UUID elementId, UUID userId);

    List<TrashRecord> findByExpirationDateLessThanEqual(Date expirationDate);

    @Modifying
    @Transactional
    @Query("DELETE FROM TrashRecord t WHERE t.access = true AND t.sharing = true AND t.manager = true")
    void deleteByAccessAndSharingAndManagerTrue();

    void deleteByElementIdAndUserId(UUID fileId, UUID userId);

    List<TrashRecord> findByUserIdAndRoot(UUID userId, boolean root);
}
