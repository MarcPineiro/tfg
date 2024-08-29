package edu.udg.tfg.FileManagement.entities.mappers;

import edu.udg.tfg.FileManagement.controlllers.responses.FileInfo;
import edu.udg.tfg.FileManagement.controlllers.responses.FolderInfo;
import edu.udg.tfg.FileManagement.entities.FolderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = FolderMapper.class)
public interface FolderMapper {

    @Mapping(source = "elementId", target = "id")
    FolderInfo map(FolderEntity source);

    @Mapping(source = "id", target = "elementId")
    FolderEntity map(FolderInfo source);

    @Mapping(source = "elementId", target = "id")
    FileInfo mapFileInfo(FolderEntity source);

    @Mapping(source = "id", target = "elementId")
    FolderEntity mapFileInfo(FolderInfo source);

    List<FolderInfo> mapFileInfo(List<FolderEntity> destination);
}
