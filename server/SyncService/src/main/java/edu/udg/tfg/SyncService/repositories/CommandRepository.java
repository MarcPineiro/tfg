package edu.udg.tfg.SyncService.repositories;

import edu.udg.tfg.SyncService.Entities.CommandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommandRepository extends JpaRepository<CommandEntity, UUID> {
    List<CommandEntity> findByUserId(UUID userId);
    void deleteByElementIdAndUserId(UUID elementId, UUID userId);
}
