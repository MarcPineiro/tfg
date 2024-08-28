package edu.udg.tfg.Trash.services;

import com.netflix.discovery.converters.Auto;
import edu.udg.tfg.Trash.controllers.requests.TrashRequest;
import edu.udg.tfg.Trash.controllers.responses.TrashResponse;
import edu.udg.tfg.Trash.entities.TrashRecord;
import edu.udg.tfg.Trash.queue.Sender;
import edu.udg.tfg.Trash.repositories.TrashRecordRepository;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TrashService {

    @Autowired
    private TrashRecordRepository trashRecordRepository;

    @Value("${access.service.name}")
    private String accessName;
    @Value("${sharing.service.name}")
    private String sharingName;
    @Value("${management.service.name}")
    private String managementName;

    @Autowired
    private Sender sender;

    public void addRecord(UUID userId, TrashRequest trashRequest) {
        TrashRecord trashRecord = new TrashRecord(userId, trashRequest.getElementId(), true);
        List<TrashRecord> records = new ArrayList<>();
        records.add(trashRecord);
        trashRequest.getIds().forEach(id -> records.add(new TrashRecord(userId, id, false)));
        trashRecordRepository.saveAll(records);
    }

    public TrashResponse getSharedFiles(UUID userId) {
        List<TrashRecord> records = trashRecordRepository.findByUserIdAndRoot(userId, true);
        TrashResponse trashResponse = new TrashResponse();
        records.forEach(r -> trashResponse.getFiles().add(r.getElementId()));
        return trashResponse;
    }

    public void confirm(UUID elementId, UUID userId, String service) {
        TrashRecord trashRecord = trashRecordRepository.findByElementIdAndUserId(elementId, userId).orElseThrow(() ->new NotFoundException("File not found"));
        if (service.equals(accessName)) {
            trashRecord.setAccess(true);
        } else if(service.equals(sharingName)) {
            trashRecord.setSharing(true);
        } else if(service.equals(managementName)) {
            trashRecord.setManager(true);
        }
        trashRecordRepository.save(trashRecord);
    }

    public void removeExpiredRecords() {
        List<TrashRecord> records = trashRecordRepository.findByExpirationDateLessThanEqual(new Date());
        records.forEach(record -> {
            sender.removeAccess(record.getUserId(), record.getElementId());
            sender.removeManagement(record.getUserId(), record.getElementId());
            sender.removeSharing(record.getUserId(), record.getElementId());
        });
    }

    public void cleanRecords() {
        trashRecordRepository.deleteByAccessAndSharingAndManagerTrue();
    }

    public void remove(UUID userId, UUID elementId) {
        trashRecordRepository.deleteByElementIdAndUserId(userId, elementId);
    }
}