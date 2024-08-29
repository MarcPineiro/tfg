package edu.udg.tfg.HistoryService.entities.mappers;

import edu.udg.tfg.FileManagement.queue.messages.RecordMessage;
import edu.udg.tfg.HistoryService.entities.HistoryRecord;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-08-29T13:54:03+0200",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 18.0.2 (Amazon.com Inc.)"
)
@Component
public class RecordMapperImpl implements RecordMapper {

    @Override
    public List<RecordMessage> map(List<HistoryRecord> destination) {
        if ( destination == null ) {
            return null;
        }

        List<RecordMessage> list = new ArrayList<RecordMessage>( destination.size() );
        for ( HistoryRecord historyRecord : destination ) {
            list.add( map( historyRecord ) );
        }

        return list;
    }

    @Override
    public RecordMessage map(HistoryRecord source) {
        if ( source == null ) {
            return null;
        }

        UUID userId = null;
        String action = null;
        String elementType = null;
        String elementName = null;
        UUID elementId = null;
        UUID parentId = null;
        String path = null;
        Date actionDate = null;

        userId = source.getUserId();
        action = source.getAction();
        elementType = source.getElementType();
        elementName = source.getElementName();
        elementId = source.getElementId();
        parentId = source.getParentId();
        path = source.getPath();
        if ( source.getActionDate() != null ) {
            actionDate = Date.from( source.getActionDate().toInstant( ZoneOffset.UTC ) );
        }

        RecordMessage recordMessage = new RecordMessage( userId, action, elementType, elementName, elementId, parentId, path, actionDate );

        return recordMessage;
    }

    @Override
    public HistoryRecord map(RecordMessage source) {
        if ( source == null ) {
            return null;
        }

        HistoryRecord historyRecord = new HistoryRecord();

        historyRecord.setUserId( source.userId() );
        historyRecord.setAction( source.action() );
        historyRecord.setElementType( source.elementType() );
        historyRecord.setElementName( source.elementName() );
        historyRecord.setElementId( source.elementId() );
        historyRecord.setParentId( source.parentId() );
        historyRecord.setPath( source.path() );
        if ( source.actionDate() != null ) {
            historyRecord.setActionDate( LocalDateTime.ofInstant( source.actionDate().toInstant(), ZoneId.of( "UTC" ) ) );
        }

        return historyRecord;
    }
}
