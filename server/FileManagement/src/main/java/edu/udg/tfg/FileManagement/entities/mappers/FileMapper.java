package edu.udg.tfg.FileManagement.entities.mappers;

import edu.udg.tfg.FileManagement.controlllers.responses.FileInfo;
import edu.udg.tfg.FileManagement.entities.FileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FileMapper {

    List<FileInfo> map(List<FileEntity> destination);

    @Mapping(source = "elementId", target = "id")
    FileInfo map(FileEntity source);


    @Mapping(source = "id", target = "elementId")
    FileEntity map(FileInfo source);


}
