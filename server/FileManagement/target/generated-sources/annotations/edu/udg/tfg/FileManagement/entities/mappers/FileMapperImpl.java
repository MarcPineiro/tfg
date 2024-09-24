package edu.udg.tfg.FileManagement.entities.mappers;

import edu.udg.tfg.FileManagement.controlllers.responses.FileInfo;
import edu.udg.tfg.FileManagement.controlllers.responses.SharedInfo;
import edu.udg.tfg.FileManagement.entities.FileEntity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-08-31T12:49:07+0200",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 18.0.2 (Amazon.com Inc.)"
)
@Component
public class FileMapperImpl implements FileMapper {

    @Override
    public List<FileInfo> map(List<FileEntity> destination) {
        if ( destination == null ) {
            return null;
        }

        List<FileInfo> list = new ArrayList<FileInfo>( destination.size() );
        for ( FileEntity fileEntity : destination ) {
            list.add( map( fileEntity ) );
        }

        return list;
    }

    @Override
    public FileInfo map(FileEntity source) {
        if ( source == null ) {
            return null;
        }

        UUID id = null;
        Date creationDate = null;
        Date lastModification = null;
        String name = null;

        id = source.getElementId();
        creationDate = source.getCreationDate();
        lastModification = source.getLastModification();
        name = source.getName();

        String owner = null;
        List<SharedInfo> sharedWith = null;

        FileInfo fileInfo = new FileInfo( id, name, creationDate, lastModification, owner, sharedWith );

        return fileInfo;
    }

    @Override
    public FileEntity map(FileInfo source) {
        if ( source == null ) {
            return null;
        }

        FileEntity fileEntity = new FileEntity();

        fileEntity.setElementId( source.getId() );
        fileEntity.setId( source.getId() );
        fileEntity.setName( source.getName() );
        fileEntity.setCreationDate( source.getCreationDate() );
        fileEntity.setLastModification( source.getLastModification() );

        return fileEntity;
    }
}
