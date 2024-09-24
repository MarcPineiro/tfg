package edu.udg.tfg.FileManagement.services;

import edu.udg.tfg.FileManagement.entities.FolderEntity;
import edu.udg.tfg.FileManagement.queue.Sender;
import edu.udg.tfg.FileManagement.queue.messages.CommandRabbit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class CommandService {
    @Autowired
    private Sender sender;

    private CommandRabbit commandRabbit(String action, String client, String path, String elementId, String userId, Date creation, Date modified, String parent) {
        return new CommandRabbit(action, userId, elementId, path, creation, modified, client,parent);
    }

    private String getPath(FolderEntity parent) {
        if(parent == null) return "/";
        return getPath(parent.getParent()) + "/" + parent.getName();
    }

    public void sendDownload(UUID elementId, String client, String userId, Date modified, FolderEntity parent) {
        sender.sendCommand(
                new CommandRabbit("download", userId, elementId.toString(), getPath(parent), new Date(), modified, client,parent.getElementId().toString()
                ));
    }

    public void sendCreate(UUID elementId, String client, String userId, Date modified, FolderEntity parent) {
        sender.sendCommand(
                new CommandRabbit("create", userId, elementId.toString(), getPath(parent), new Date(), modified, client,parent.getElementId().toString()
                ));
    }

    public void sendUpdate(UUID elementId, String client, String userId, Date modified, FolderEntity parent) {
        sender.sendCommand(
                new CommandRabbit("update", userId, elementId.toString(), getPath(parent), new Date(), modified, client,parent.getElementId().toString()
                ));
    }

    public void sendDelete(UUID elementId, String client, String userId, Date modified, FolderEntity parent) {
        sender.sendCommand(
                new CommandRabbit("delete", userId, elementId.toString(), getPath(parent), new Date(), modified, client,parent.getElementId().toString()
                ));
    }
}
