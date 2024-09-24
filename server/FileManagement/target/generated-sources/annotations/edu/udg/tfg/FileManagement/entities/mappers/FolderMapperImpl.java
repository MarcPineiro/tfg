package edu.udg.tfg.FileManagement.entities.mappers;

import edu.udg.tfg.FileManagement.controlllers.responses.FileInfo;
import edu.udg.tfg.FileManagement.controlllers.responses.FolderInfo;
import edu.udg.tfg.FileManagement.controlllers.responses.SharedInfo;
import edu.udg.tfg.FileManagement.entities.FileEntity;
import edu.udg.tfg.FileManagement.entities.FolderEntity;
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
public class FolderMapperImpl implements FolderMapper {

    @Override
    public FolderInfo map(FolderEntity source) {
        if ( source == null ) {
            return null;
        }

        FolderInfo folderInfo = new FolderInfo();

        folderInfo.setId( source.getElementId() );
        folderInfo.setName( source.getName() );
        folderInfo.setCreationDate( source.getCreationDate() );
        folderInfo.setLastModification( source.getLastModification() );
        folderInfo.setFiles( fileEntityListToFileInfoList( source.getFiles() ) );

        return folderInfo;
    }

    @Override
    public FolderEntity map(FolderInfo source) {
        if ( source == null ) {
            return null;
        }

        FolderEntity folderEntity = new FolderEntity();

        folderEntity.setId( source.getId() );
        folderEntity.setName( source.getName() );
        folderEntity.setFiles( fileInfoListToFileEntityList( source.getFiles() ) );
        folderEntity.setLastModification( source.getLastModification() );
        folderEntity.setCreationDate( source.getCreationDate() );

        return folderEntity;
    }

    @Override
    public FileInfo mapFileInfo(FolderEntity source) {
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
    public FolderEntity mapFileInfo(FolderInfo source) {
        if ( source == null ) {
            return null;
        }

        FolderEntity folderEntity = new FolderEntity();

        folderEntity.setId( source.getId() );
        folderEntity.setName( source.getName() );
        folderEntity.setFiles( fileInfoListToFileEntityList( source.getFiles() ) );
        folderEntity.setLastModification( source.getLastModification() );
        folderEntity.setCreationDate( source.getCreationDate() );

        return folderEntity;
    }

    @Override
    public List<FolderInfo> mapFileInfo(List<FolderEntity> destination) {
        if ( destination == null ) {
            return null;
        }

        List<FolderInfo> list = new ArrayList<FolderInfo>( destination.size() );
        for ( FolderEntity folderEntity : destination ) {
            list.add( map( folderEntity ) );
        }

        return list;
    }

    protected FileInfo fileEntityToFileInfo(FileEntity fileEntity) {
        if ( fileEntity == null ) {
            return null;
        }

        Date creationDate = null;
        Date lastModification = null;
        UUID id = null;
        String name = null;

        creationDate = fileEntity.getCreationDate();
        lastModification = fileEntity.getLastModification();
        id = fileEntity.getId();
        name = fileEntity.getName();

        String owner = null;
        List<SharedInfo> sharedWith = null;

        FileInfo fileInfo = new FileInfo( id, name, creationDate, lastModification, owner, sharedWith );

        return fileInfo;
    }

    protected List<FileInfo> fileEntityListToFileInfoList(List<FileEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<FileInfo> list1 = new ArrayList<FileInfo>( list.size() );
        for ( FileEntity fileEntity : list ) {
            list1.add( fileEntityToFileInfo( fileEntity ) );
        }

        return list1;
    }

    protected FileEntity fileInfoToFileEntity(FileInfo fileInfo) {
        if ( fileInfo == null ) {
            return null;
        }

        FileEntity fileEntity = new FileEntity();

        fileEntity.setId( fileInfo.getId() );
        fileEntity.setName( fileInfo.getName() );
        fileEntity.setCreationDate( fileInfo.getCreationDate() );
        fileEntity.setLastModification( fileInfo.getLastModification() );

        return fileEntity;
    }

    protected List<FileEntity> fileInfoListToFileEntityList(List<FileInfo> list) {
        if ( list == null ) {
            return null;
        }

        List<FileEntity> list1 = new ArrayList<FileEntity>( list.size() );
        for ( FileInfo fileInfo : list ) {
            list1.add( fileInfoToFileEntity( fileInfo ) );
        }

        return list1;
    }
}
