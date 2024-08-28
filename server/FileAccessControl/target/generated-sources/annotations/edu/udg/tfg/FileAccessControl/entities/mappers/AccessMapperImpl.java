package edu.udg.tfg.FileAccessControl.entities.mappers;

import edu.udg.tfg.FileAccessControl.controllers.requests.AccessRequest;
import edu.udg.tfg.FileAccessControl.controllers.responses.AccessResponse;
import edu.udg.tfg.FileAccessControl.entities.AccessRule;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-08-27T23:30:49+0200",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 18.0.2 (Amazon.com Inc.)"
)
@Component
public class AccessMapperImpl implements AccessMapper {

    @Override
    public List<AccessResponse> map(List<AccessRule> destination) {
        if ( destination == null ) {
            return null;
        }

        List<AccessResponse> list = new ArrayList<AccessResponse>( destination.size() );
        for ( AccessRule accessRule : destination ) {
            list.add( map( accessRule ) );
        }

        return list;
    }

    @Override
    public AccessResponse map(AccessRule source) {
        if ( source == null ) {
            return null;
        }

        UUID fieldId = null;
        int accessType = 0;
        UUID userId = null;

        fieldId = source.getElementId();
        accessType = map( source.getAccessType() );
        userId = source.getUserId();

        AccessResponse accessResponse = new AccessResponse( accessType, userId, fieldId );

        return accessResponse;
    }

    @Override
    public AccessRule map(AccessRequest source) {
        if ( source == null ) {
            return null;
        }

        AccessRule accessRule = new AccessRule();

        accessRule.setUserId( source.getUserId() );
        accessRule.setElementId( source.getElementId() );
        accessRule.setAccessType( map( source.getAccessType() ) );

        return accessRule;
    }
}
