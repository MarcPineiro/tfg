package edu.udg.tfg.HistoryService.services;

import edu.udg.tfg.HistoryService.config.RabbitConfig;
import edu.udg.tfg.HistoryService.entities.HistoryRecord;
import edu.udg.tfg.HistoryService.repositories.HistoryRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class HistoryService {

    @Autowired
    private HistoryRepository historyRepository;

    public Page<HistoryRecord> getHistoryByUser(Long userId, String sortOrder, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        if ("desc".equalsIgnoreCase(sortOrder)) {
            return historyRepository.findByUserIdOrderByActionDateDesc(userId, pageRequest);
        } else {
            return historyRepository.findByUserIdOrderByActionDateAsc(userId, pageRequest);
        }
    }

    public void saveRecord(HistoryRecord record) {
        historyRepository.save(record);
    }
}
