package edu.udg.tfg.FileAccessControl.entities.mappers;

import edu.udg.tfg.FileAccessControl.controllers.requests.AccessRequest;
import edu.udg.tfg.FileAccessControl.controllers.responses.AccessResponse;
import edu.udg.tfg.FileAccessControl.entities.AccessRule;
import edu.udg.tfg.FileAccessControl.entities.AccessType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccessMapper {

    List<AccessResponse> map(List<AccessRule> destination);

    @Mapping(source = "elementId", target = "fieldId")
    AccessResponse map(AccessRule source);
    AccessRule map(AccessRequest source);

    default AccessType map(int value) {
        return AccessType.values()[value];
    }
    default int map(AccessType value) {
        return value.ordinal();
    }
}
