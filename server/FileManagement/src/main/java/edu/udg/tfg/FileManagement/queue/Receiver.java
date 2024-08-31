package edu.udg.tfg.FileManagement.queue;

import edu.udg.tfg.FileManagement.config.RabbitConfig;
import edu.udg.tfg.FileManagement.controlllers.responses.FolderStructure;
import edu.udg.tfg.FileManagement.feignClients.fileAccess.AccessType;
import edu.udg.tfg.FileManagement.queue.messages.DeleteAccess;
import edu.udg.tfg.FileManagement.queue.messages.DeleteElemtn;
import edu.udg.tfg.FileManagement.services.ElementService;
import edu.udg.tfg.FileManagement.services.FileAccessService;
import edu.udg.tfg.FileManagement.services.FileService;
import edu.udg.tfg.FileManagement.services.FolderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@Component
public class Receiver {

    @Autowired
    ElementService elementService;

    @Autowired
    FileService fileService;

    @Autowired
    FolderService folderService;

    @Autowired
    FileAccessService fileAccessService;

    @RabbitListener(queues = RabbitConfig.DELETE_USER_FM)
    public void receiveMessage(final UUID userId) {
        fileService.deleteByUserId(userId);
        folderService.deleteByUserId(userId);
    }

    @RabbitListener(queues = RabbitConfig.DELETE_ELEMENT)
    public void deleteElement(final DeleteElemtn deleteElemtn) {
        boolean isFolder = elementService.isFolder(deleteElemtn.elementId());
        fileAccessService.checkAccessElement(deleteElemtn.userId(), deleteElemtn.elementId(), isFolder, AccessType.ADMIN);
        if(isFolder) {
            folderService.removeFolder(deleteElemtn.elementId());
        } else {
            fileService.deleteFile(deleteElemtn.elementId());
        }
    }


}