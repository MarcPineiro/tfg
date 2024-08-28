package edu.udg.tfg.UserAuthentication;

import edu.udg.tfg.UserAuthentication.entities.UserEntity;
import edu.udg.tfg.UserAuthentication.feignClients.fileManagement.FileManagementClient;
import edu.udg.tfg.UserAuthentication.feignClients.userManagement.UserManagementClient;
import edu.udg.tfg.UserAuthentication.feignClients.userManagement.UserRequest;
import edu.udg.tfg.UserAuthentication.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class DataInitializer {

    @Bean
    public ApplicationRunner initializer(UserRepository repository, PasswordEncoder passwordEncoder, FileManagementClient fileManagementClient, UserManagementClient userManagementClient) {
        return args -> {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername("test");
            userEntity.setPassword(passwordEncoder.encode("test"));
            userEntity = repository.save(userEntity);
            UserRequest userRequest = new UserRequest();
            userRequest.setId(userEntity.getId());
            userRequest.setLastModifiedDate(new Date());
            userRequest.setLastName("testLast");
            userRequest.setFirstName("testFirst");
            userRequest.setEmail("test@test.com");
            userRequest.setCreatedDate(new Date());
            try {
                userManagementClient.creteUser(userEntity.getId(), userRequest);
            } catch (Exception e) {
                // Nothing
            }
            try {
                fileManagementClient.createRoot(userEntity.getId());
            } catch (Exception e) {
                //e.printStackTrace();
            }
            boolean test = true;
        };
    }
}