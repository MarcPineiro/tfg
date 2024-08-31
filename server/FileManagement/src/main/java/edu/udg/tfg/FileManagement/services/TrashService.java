package edu.udg.tfg.FileManagement.services;

import edu.udg.tfg.FileManagement.controlllers.responses.FolderInfo;
import edu.udg.tfg.FileManagement.controlllers.responses.FolderStructure;
import edu.udg.tfg.FileManagement.entities.ElementEntity;
import edu.udg.tfg.FileManagement.entities.FileEntity;
import edu.udg.tfg.FileManagement.entities.FolderEntity;
import edu.udg.tfg.FileManagement.entities.mappers.FileMapper;
import edu.udg.tfg.FileManagement.entities.mappers.FolderMapper;
import edu.udg.tfg.FileManagement.feignClients.trash.TrashRequest;
import edu.udg.tfg.FileManagement.feignClients.trash.TrashResponse;
import edu.udg.tfg.FileManagement.feignClients.trash.TrashClient;
import edu.udg.tfg.FileManagement.feignClients.userAuth.UserAuthenticationClient;
import edu.udg.tfg.FileManagement.queue.Sender;
import edu.udg.tfg.FileManagement.repositories.FileRepository;
import edu.udg.tfg.FileManagement.repositories.FolderRepository;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TrashService {
    @Autowired
    private TrashClient trashClient;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FileAccessService fileAccessService;

    @Autowired
    private UserAuthenticationClient userAuthenticationClient;

    @Autowired
    private Sender sender;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private FolderMapper folderMapper;

    public FolderInfo getRootByUserId(UUID userId) {
        TrashResponse filesSharedResponse = trashClient.getTrashFiles(userId.toString());
        String userName = userAuthenticationClient.getUserName(userId);
        FolderInfo folderInfo = new FolderInfo(null, "shared", new Date(), new Date(), userName);
        folderInfo.setSubfolders(null);
        folderInfo.setFiles(null);
        for (UUID elementId : filesSharedResponse.getFiles()) {
            Optional<FileEntity> file = fileRepository.findByElementId(new ElementEntity(elementId));
            if(file.isPresent()) {
                folderInfo.getFiles().add(fileMapper.map(file.get()));
            }
            else {
                Optional<FolderEntity> folder = folderRepository.findByElementId(new ElementEntity(elementId));
                folder.ifPresent(folderEntity -> folderInfo.getSubfolders().add(folderMapper.mapFileInfo(folderEntity)));
            }
        }
        return folderInfo;
    }

    public void remove(UUID elementId, UUID userId, boolean isFolder) {
        trashClient.addRecord(userId.toString(), trashRequest(elementId, isFolder));
    }

    public void restore(UUID elementId, UUID userId) {
        trashClient.restoreFile(userId.toString(), elementId.toString());
    }

    private TrashRequest trashRequest(UUID elementId, boolean isFolder) {
        TrashRequest trashRequest = new TrashRequest();
        trashRequest.setElementId(elementId);
        trashRequest.setIds(new ArrayList<>());
        if(isFolder) {
            FolderEntity folder = folderRepository.findByElementId(new ElementEntity(elementId)).orElseThrow(() -> new NotFoundException("Folder not found"));
            folder.getFiles().forEach(file -> trashRequest.getIds().add(file.getElementId()));
            folder.getChildren().forEach(folder1 -> trashRequest.getIds().add(folder1.getElementId()));
        }
        return trashRequest;
    }

    public FolderStructure getFolderStructure(UUID userId) {
        FolderInfo folderInfo = getRootByUserId(userId);
        return setStructure(folderInfo);
    }

    private FolderStructure setStructure(FolderInfo folder) {
        FolderStructure folderStructure = new FolderStructure(folder.getId(), folder.getName());
        folder.getSubfolders().forEach(folder1 -> {
            Optional<FolderEntity> op = folderRepository.findByElementIdAndDeleted(new ElementEntity(folder1.getId()), true);
            if (op.isPresent()) {
                FolderInfo folderInfo = folderMapper.map(op.get());
                folderStructure.getSubfolders().add(setStructure(folderInfo));
            }
        });
        return folderStructure;
    }
}
