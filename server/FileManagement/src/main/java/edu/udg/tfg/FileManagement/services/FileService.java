package edu.udg.tfg.FileManagement.services;

import edu.udg.tfg.FileManagement.controlllers.responses.FileInfo;
import edu.udg.tfg.FileManagement.entities.ElementEntity;
import edu.udg.tfg.FileManagement.entities.FileEntity;
import edu.udg.tfg.FileManagement.entities.FolderEntity;
import edu.udg.tfg.FileManagement.entities.mappers.FileMapper;
import edu.udg.tfg.FileManagement.feignClients.fileAccess.AccessType;
import edu.udg.tfg.FileManagement.queue.Sender;
import edu.udg.tfg.FileManagement.repositories.ElementRepository;
import edu.udg.tfg.FileManagement.repositories.FileRepository;
import edu.udg.tfg.FileManagement.repositories.FolderRepository;
import edu.udg.tfg.FileManagement.utils.FileUtil;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private ElementRepository elementRepository;

    @Autowired
    private FileAccessService fileAccessService;

    @Autowired
    private Sender sender;

    @Autowired
    private FileUtil fileUtil;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private SharedService sharedService;

    public FileEntity createFile(String name, String contentType, Long size, UUID folderId, UUID userId) {
        FolderEntity folder = folderRepository.findByElementId(new ElementEntity(folderId)).orElseThrow(() -> new NotFoundException("Folder not found"));
        FileEntity file = new FileEntity();
        file.setName(checkName(name, folder));
        file.setSize(size);
        file.setParent(folder);
        file.setUserId(userId);
        ElementEntity id = new ElementEntity();
        id.setFolder(false);
        id = elementRepository.save(id);
        file.setElementId(id.getId());
        FileEntity fileEntity = fileRepository.save(file);
        fileAccessService.addFileAccess(fileEntity.getElementId(), userId, AccessType.ADMIN);
        return fileEntity;
    }

    private String checkName(String name, FolderEntity parent) {
        List<FileEntity> folders = fileRepository.findByParentAndName(parent, name);
        if(folders.isEmpty()) return name;
        else return name + "(" + folders.size() + ")";
    }

    public FileEntity updateFile(UUID fileId, String name) {
        FileEntity file = fileRepository.findByElementId(new ElementEntity(fileId)).orElseThrow(() -> new NotFoundException("File not found"));
        file.setName(name);
        file.setLastModification(new Date());
        return fileRepository.save(file);
    }

    public void deleteFile(UUID fileId) {
        FileEntity file = fileRepository.findByElementId(new ElementEntity(fileId)).orElseThrow(() -> new NotFoundException("File not found"));
        fileAccessService.deleteFileAccessAsincornous(fileId, file.getUserId());
        fileRepository.delete(file);
    }

    public void setDeleteFile(UUID fileId) {
        FileEntity file = fileRepository.findByElementId(new ElementEntity(fileId)).orElseThrow(() -> new NotFoundException("File not found"));
        file.setDeleted(true);
        fileRepository.save(file);
    }

    public void moveFile(UUID fileId, UUID folderId) {
        Optional<FileEntity> file = fileRepository.findByElementId(new ElementEntity(fileId));
        FileEntity fileEntity = file.orElseThrow(() -> new NotFoundException("File not found"));
        Optional<FolderEntity> folder = folderRepository.findByElementId(new ElementEntity(folderId));
        fileEntity.setParent(folder.orElseThrow(() -> new NotFoundException("File not found")));
        fileRepository.save(fileEntity);
    }

    public FileEntity getFile(UUID fileId) {
        return fileRepository.findByElementId(new ElementEntity(fileId)).orElseThrow(() -> new NotFoundException("File not found"));
    }

    public FileEntity getFolderByElementId(UUID elementId, boolean deleted) {
        return fileRepository.findByElementIdAndDeleted(new ElementEntity(elementId), deleted).orElseThrow(() -> new NotFoundException("File not found"));
    }

    public void restore(UUID elementId) {
        FileEntity file = fileRepository.findByElementId(new ElementEntity(elementId)).orElseThrow(() -> new NotFoundException("Folder not found"));
        file.setDeleted(false);
        fileRepository.save(file);
    }

    public FileInfo buildFileInfo(FileEntity file) {
        FileInfo fileInfo = fileMapper.map(file);
        fileInfo.setSharedWith(sharedService.getShareds(fileInfo.getId()));
        return fileInfo;
    }

    public void deleteByUserId(UUID userId) {
        fileRepository.deleteByUserId(userId);
    }
}