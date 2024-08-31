package edu.udg.tfg.FileManagement.services;

import com.ctc.wstx.util.ElementId;
import edu.udg.tfg.FileManagement.controlllers.responses.FileInfo;
import edu.udg.tfg.FileManagement.controlllers.responses.FolderStructure;
import edu.udg.tfg.FileManagement.controlllers.responses.SharedInfo;
import edu.udg.tfg.FileManagement.entities.ElementEntity;
import edu.udg.tfg.FileManagement.entities.FileEntity;
import edu.udg.tfg.FileManagement.entities.FolderEntity;
import edu.udg.tfg.FileManagement.entities.mappers.FolderMapper;
import edu.udg.tfg.FileManagement.feignClients.fileAccess.AccessType;
import edu.udg.tfg.FileManagement.queue.Sender;
import edu.udg.tfg.FileManagement.repositories.ElementRepository;
import edu.udg.tfg.FileManagement.repositories.FileRepository;
import edu.udg.tfg.FileManagement.repositories.FolderRepository;
import edu.udg.tfg.FileManagement.utils.FileUtil;
import edu.udg.tfg.FileManagement.utils.ZipUtil;
import jakarta.ws.rs.ForbiddenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.ws.rs.NotFoundException;

@Service
public class FolderService {

    @Value("${file.zip.path}")
    private String zipPath;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ElementRepository elementRepository;

    @Autowired
    private FileAccessService fileAccessService;

    @Autowired
    private Sender sender;

    @Autowired
    private FileUtil fileUtil;
    @Autowired
    private FolderMapper folderMapper;

    @Autowired
    private SharedService sharedService;

    public FolderEntity createFolder(String name, FolderEntity parent, UUID userId) {
        FolderEntity folderEntity = new FolderEntity();
        folderEntity.setName(checkName(name, parent));
        folderEntity.setUserId(userId);
        ElementEntity id = new ElementEntity();
        id.setFolder(true);
        id = elementRepository.save(id);
        folderEntity.setElementId(id.getId());
        FolderEntity folder = folderRepository.save(folderEntity);
        fileAccessService.addFileAccess(folder.getElementId(), userId, AccessType.ADMIN);
        return folder;
    }

    private String checkName(String name, FolderEntity parent) {
        if(parent == null) return name;
        List<FolderEntity> folders = folderRepository.findByParentAndName(parent, name);
        if(folders.isEmpty()) return name;
        else return name + "(" + folders.size() + ")";
    }

    public void updateFolderMetadata(UUID folderId, String name) {
        FolderEntity folder = folderRepository.findByElementId(new ElementEntity(folderId))
                .orElseThrow(() -> new NotFoundException("Folder not found"));
        folder.setName(name);
        folderRepository.save(folder);
    }

    public Resource downloadFolder(UUID folderId) {
        FolderEntity folder = folderRepository.findByElementId(new ElementEntity(folderId))
                .orElseThrow(() -> new NotFoundException("Folder not found"));

        return ZipUtil.createZipFromFolder(folder, zipPath, fileUtil);
    }

    @Transactional
    public void removeFolder(UUID folderId) {
        FolderEntity folder = folderRepository.findByElementId(new ElementEntity(folderId))
                .orElseThrow(() -> new NotFoundException("Folder not found"));
        removeFolderRecursively(folder);
    }

    private void removeFolderRecursively(FolderEntity folder) {
        //sender.deleteAccess(folder.getElementId(), folder.getUserId());
        for (FolderEntity child : folder.getChildren()) {
            removeFolderRecursively(child);
        }
        //folder.getFiles().forEach(f -> sender.deleteAccess(f.getElementId(), f.getUserId()));
        fileRepository.deleteAll(folder.getFiles());
        folderRepository.delete(folder);
    }

    @Transactional
    public void setRemoveFolder(UUID folderId) {
        FolderEntity folder = folderRepository.findByElementId(new ElementEntity(folderId))
                .orElseThrow(() -> new NotFoundException("Folder not found"));
        setRemoveFolderRecursively(folder);
    }

    private void setRemoveFolderRecursively(FolderEntity folder) {
        folder.setDeleted(true);
        for (FolderEntity child : folder.getChildren()) {
            removeFolderRecursively(child);
        }
        folder.getFiles().forEach(f -> {
            f.setDeleted(true);
            fileRepository.save(f);
        });
        folderRepository.save(folder);
    }

    public void moveFile(UUID entityId, UUID folderId) {
        Optional<FolderEntity> file = folderRepository.findByElementId(new ElementEntity(entityId));
        FolderEntity fileEntity = file.orElseThrow(() -> new NotFoundException("Folder not found"));
        FolderEntity folder = folderRepository.findByElementId(new ElementEntity(folderId)).orElseThrow(() -> new NotFoundException("Folder not found"));
        checkNoAncestor(fileEntity, folder);
        fileEntity.setParent(folder);
        folderRepository.save(fileEntity);
    }

    private void checkNoAncestor(FolderEntity fileEntity, FolderEntity folder) {
        if(folder == null) return;
        if(fileEntity.getId() == folder.getId()) throw new ForbiddenException("The movement is illegal.");
        checkNoAncestor(fileEntity, folder.getParent());
    }

    public List<FileEntity> getFilesByFolder(UUID folderId) {
        FolderEntity folder = folderRepository.findByElementId(new ElementEntity(folderId)).orElseThrow(() -> new NotFoundException("Folder not found"));
        return folder.getFiles();
    }

    public FolderEntity getFolderById(UUID folderId) {
        return folderRepository.findByElementId(new ElementEntity(folderId)).orElseThrow(() -> new NotFoundException("Folder not found"));
    }

    public FolderEntity getRootFolder(UUID userId, boolean deleted) {
        return folderRepository.findByUserIdAndDeletedAndParentIsNull(userId, deleted).orElseThrow(() -> new NotFoundException("Folder not found"));
    }

    public FolderEntity getFolderByElementId(UUID elementId, boolean deleted) {
        return folderRepository.findByElementIdAndDeleted(new ElementEntity(elementId), deleted).orElseThrow(() -> new NotFoundException("Folder not found"));
    }

    public FolderStructure getFolderStructure(UUID userId) {
        FolderEntity folder = getRootFolder(userId, false);
        return setStructure(folder);
    }

    private FolderStructure setStructure(FolderEntity folder) {
        FolderStructure folderStructure = new FolderStructure(folder.getElementId(), folder.getName());
        folder.getChildren().stream().filter(f -> !f.getDeleted()).forEach(folder1 -> folderStructure.getSubfolders().add(setStructure(folder1)));
        return folderStructure;
    }

    public void restore(UUID elementId) {
        FolderEntity folder = folderRepository.findByElementId(new ElementEntity(elementId)).orElseThrow(() -> new NotFoundException("Folder not found"));
        folder.setDeleted(false);
        folderRepository.save(folder);
    }

    public void createRootFolder(UUID userId) {
        if(folderRepository.findByUserIdAndDeletedAndParentIsNull(userId, false).isPresent()) {
            throw new ForbiddenException();
        }
        createFolder("", null, userId);
    }

    public FileInfo buildFileInfo(FolderEntity folder) {
        FileInfo fileInfo = folderMapper.mapFileInfo(folder);
        fileInfo.setSharedWith(sharedService.getShareds(fileInfo.getId()));
        return fileInfo;
    }

    public void deleteByUserId(UUID userId) {
        folderRepository.deleteByUserId(userId);
    }
}
