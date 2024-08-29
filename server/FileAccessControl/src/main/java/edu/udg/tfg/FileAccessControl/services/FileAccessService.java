package edu.udg.tfg.FileAccessControl.services;// FileAccessService.java

import edu.udg.tfg.FileAccessControl.entities.AccessRule;
import edu.udg.tfg.FileAccessControl.repositories.AccessRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileAccessService {

    @Autowired
    private AccessRuleRepository accessRuleRepository;

    public void addFileAccess(AccessRule fileAccess) {
        accessRuleRepository.save(fileAccess);
    }

    public void deleteFileAccess(UUID fileId, UUID userId) {
        accessRuleRepository.deleteByElementIdAndUserId(fileId, userId);
    }

    public AccessRule getFileAccess(UUID fileId, UUID userId) {
        Optional<AccessRule> accessRule = accessRuleRepository.findByElementIdAndUserId(fileId, userId);
        return accessRule.orElseGet(AccessRule::new);
    }

    public List<AccessRule> getFileAccessByUserId(UUID userId) {
        return accessRuleRepository.findByUserId(userId);
    }

    public List<AccessRule> getFileAccessByFileId(UUID fileId) {
        return accessRuleRepository.findByElementId(fileId);
    }

    public int getFileAccessType(UUID fileId, UUID userId) {
        Optional<AccessRule> accessRule = accessRuleRepository.findByElementIdAndUserId(fileId, userId);
        return accessRule.map(rule -> rule.getAccessType().ordinal()).orElseGet(() -> 0);
    }

    // Other methods for folder access, etc.
}
