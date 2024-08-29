package edu.udg.tfg.HistoryService.queue;

import edu.udg.tfg.FileManagement.queue.messages.RecordMessage;
import edu.udg.tfg.HistoryService.config.RabbitConfig;
import edu.udg.tfg.HistoryService.entities.mappers.RecordMapper;
import edu.udg.tfg.HistoryService.services.HistoryService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Receiver {

    @Autowired
    HistoryService historyService;

    @Autowired
    RecordMapper recordMapper;

    @RabbitListener(queues = RabbitConfig.HISTORY_QUEUE)
    public void deleteElement(final RecordMessage recordMessage) {
        historyService.saveRecord(recordMapper.map(recordMessage));
    }


}