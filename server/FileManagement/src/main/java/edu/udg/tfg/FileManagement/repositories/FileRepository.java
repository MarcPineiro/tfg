package edu.udg.tfg.FileManagement.repositories;

import edu.udg.tfg.FileManagement.entities.ElementEntity;
import edu.udg.tfg.FileManagement.entities.FileEntity;
import edu.udg.tfg.FileManagement.entities.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, UUID> {

    List<FileEntity> findByParent(FolderEntity folder);

    Optional<FileEntity> findByElementId(ElementEntity elementId);

    Optional<FileEntity> findByElementIdAndDeleted(ElementEntity elementEntity, boolean deleted);

    List<FileEntity> findByParentAndName(FolderEntity parent, String name);

    void deleteByUserId(UUID userId);
}