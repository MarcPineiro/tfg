package edu.udg.tfg.HistoryService.entities.mappers;


import edu.udg.tfg.FileManagement.queue.messages.RecordMessage;
import edu.udg.tfg.HistoryService.entities.HistoryRecord;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecordMapper {

    List<RecordMessage> map(List<HistoryRecord> destination);

    RecordMessage map(HistoryRecord source);
    HistoryRecord map(RecordMessage source);
}
