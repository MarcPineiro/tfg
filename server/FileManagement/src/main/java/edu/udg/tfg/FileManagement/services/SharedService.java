package edu.udg.tfg.FileManagement.services;

import edu.udg.tfg.FileManagement.controlllers.requests.ShareRequest;
import edu.udg.tfg.FileManagement.controlllers.responses.FolderInfo;
import edu.udg.tfg.FileManagement.controlllers.responses.FolderStructure;
import edu.udg.tfg.FileManagement.controlllers.responses.SharedInfo;
import edu.udg.tfg.FileManagement.entities.ElementEntity;
import edu.udg.tfg.FileManagement.entities.FileEntity;
import edu.udg.tfg.FileManagement.entities.FolderEntity;
import edu.udg.tfg.FileManagement.entities.mappers.FileMapper;
import edu.udg.tfg.FileManagement.entities.mappers.FolderMapper;
import edu.udg.tfg.FileManagement.feignClients.fileAccess.AccessType;
import edu.udg.tfg.FileManagement.feignClients.fileShare.FileShareClient;
import edu.udg.tfg.FileManagement.feignClients.fileShare.FilesSharedResponse;
import edu.udg.tfg.FileManagement.feignClients.fileShare.SharedRequest;
import edu.udg.tfg.FileManagement.feignClients.fileShare.UsersSharedResponse;
import edu.udg.tfg.FileManagement.feignClients.userAuth.UserAuthenticationClient;
import edu.udg.tfg.FileManagement.queue.Sender;
import edu.udg.tfg.FileManagement.repositories.FileRepository;
import edu.udg.tfg.FileManagement.repositories.FolderRepository;
import feign.FeignException;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

//TODO: generate share url

@Service
public class SharedService {
    @Autowired
    private FileShareClient fileShareClient;

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
    private FolderMapper folderMapper;
    @Autowired
    private FileMapper fileMapper;

    public FolderInfo getRootByUserId(UUID userId) {
        FilesSharedResponse filesSharedResponse = fileShareClient.getSharedFiles(userId.toString());
        String userName = userAuthenticationClient.getUserName(userId);
        FolderInfo folderInfo = new FolderInfo(null, "shared", new Date(), new Date(), userName);
        folderInfo.setSubfolders(null);
        folderInfo.setFiles(null);
        if(filesSharedResponse.getFiles() == null) return folderInfo;
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

    public void share(ShareRequest shareRequest, UUID userId) {
        try {
            UUID sharedUserId = userAuthenticationClient.getId(shareRequest.getShareUsername());
            setShared(shareRequest);
            fileShareClient.shareFile(userId.toString(), sharedRequest(shareRequest));
            fileAccessService.addFileAccessAssincronous(shareRequest.getElementId(), sharedUserId, AccessType.READ);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("User not found for the provided username: " + shareRequest.getShareUsername());
        } catch (FeignException e) {
            throw new RuntimeException("An error occurred while communicating with the User Management service", e);
        }
    }

    public void revokeShare(UUID elementId, UUID userId, boolean isFolder) {
        if (!isFolder) {
            Optional<FileEntity> file = fileRepository.findByElementId(new ElementEntity(elementId));
            if (file.isPresent()) {
                fileShareClient.revokeSharedFile(file.get().getElementId().toString(), userId.toString());
                setSharedFile(file.get().getElementId(), false);
            }
        } else {
            Optional<FolderEntity> folder = folderRepository.findByElementId(new ElementEntity(elementId));
            if (folder.isPresent()) {
                fileShareClient.revokeSharedFile(folder.get().getElementId().toString(), userId.toString());
                setSharedFolder(folder.get().getElementId(), false);
            }
        }
    }

    private void setShared(ShareRequest shareRequest) {
        if(shareRequest.isFile())
            setSharedFile(shareRequest.getElementId(), true);
        else {
            setSharedFolder(shareRequest.getElementId(), true);
        }
    }

    private void setSharedFile(UUID id, boolean shared) {
        Optional<FileEntity> file = fileRepository.findByElementId(new ElementEntity(id));
        if(file.isPresent()) {
            file.get().setShared(shared);
            fileRepository.save(file.get());
        } else {
            throw new NotFoundException();
        }
    }

    private void setSharedFolder(UUID id, boolean shared) {
        Optional<FolderEntity> folder = folderRepository.findByElementId(new ElementEntity(id));
        if (folder.isPresent()) {
            folder.get().setShared(shared);
            folderRepository.save(folder.get());
        } else {
            throw new NotFoundException();
        }
    }

    private SharedRequest sharedRequest(ShareRequest shareRequest) {
        SharedRequest sharedRequest = new SharedRequest();
        FolderEntity parent = null;
        if(shareRequest.isFile()) {
            Optional<FileEntity> fileEntity = fileRepository.findByElementId(new ElementEntity(shareRequest.getElementId()));
            if (fileEntity.isPresent()) {
                sharedRequest.setElementId(fileEntity.get().getElementId());
                parent = fileEntity.get().getParent();
            }
        } else {
            Optional<FolderEntity> folder = folderRepository.findByElementId(new ElementEntity(shareRequest.getElementId()));
            if(folder.isPresent()) {
                sharedRequest.setElementId(folder.get().getElementId());
                List<UUID> ids = new ArrayList<>();
                for(FileEntity file : folder.get().getFiles()) {
                    ids.add(file.getElementId());
                }
                sharedRequest.setFiles(ids);
                parent = folder.get().getParent();
            }
        }
        sharedRequest.setRoot(isRoot(parent));
        return sharedRequest;
    }

    private boolean isRoot(FolderEntity parent) {
        if(parent == null) {
            return true;
        }
        return !parent.getShared() && isRoot(parent);
    }

    public FolderStructure getFolderStructure(UUID userId) {
        FolderInfo root = getRootByUserId(userId);
        return setStructure(root);
    }

    private FolderStructure setStructure(FolderInfo folder) {
        FolderStructure folderStructure = new FolderStructure(folder.getId(), folder.getName());
        folder.getSubfolders().forEach(folder1 -> {
            Optional<FolderEntity> op = folderRepository.findByElementIdAndDeleted(new ElementEntity(folder1.getId()), false);
            if (op.isPresent()) {
                FolderInfo folderInfo = folderMapper.map(op.get());
                folderStructure.getSubfolders().add(setStructure(folderInfo));
            }
        });
        return folderStructure;
    }

    public List<SharedInfo> getShareds(UUID id) {
        List<SharedInfo> res = new ArrayList<>();
        UsersSharedResponse usersSharedResponse = fileShareClient.getUsersShared(id);
        usersSharedResponse.getUsers().forEach( userIdResp -> {
            SharedInfo sharedInfo = new SharedInfo();
            sharedInfo.setUsername(userAuthenticationClient.getUserName(userIdResp));
            sharedInfo.setUserId(userIdResp.toString());
            sharedInfo.setAccessLevel(fileAccessService.getFileAccessLevelByFileIdAndUserId(id, userIdResp));
            res.add(sharedInfo);
        });
        return res;
    }
}
