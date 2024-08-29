package edu.udg.tfg.FileManagement;

import edu.udg.tfg.FileManagement.entities.FolderEntity;
import edu.udg.tfg.FileManagement.repositories.FolderRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DataInitializer {

    @Bean
    public ApplicationRunner initializer(FolderRepository folderRepository) {
        return args -> {
            FolderEntity folder = new FolderEntity();
            folder.setName("");
            folder.setUserId(UUID.fromString("85e16d02-8522-438b-a8c2-da92550aa4e8"));
            folder.setParent(null);
            folderRepository.save(folder);
        };
    }
}