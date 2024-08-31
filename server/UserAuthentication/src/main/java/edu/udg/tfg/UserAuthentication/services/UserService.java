package edu.udg.tfg.UserAuthentication.services;

import edu.udg.tfg.UserAuthentication.controllers.requests.UserRegisterRequest;
import edu.udg.tfg.UserAuthentication.entities.UserEntity;
import edu.udg.tfg.UserAuthentication.feignClients.fileManagement.FileManagementClient;
import edu.udg.tfg.UserAuthentication.feignClients.userManagement.UserManagementClient;
import edu.udg.tfg.UserAuthentication.feignClients.userManagement.UserRequest;
import edu.udg.tfg.UserAuthentication.repositories.UserRepository;
import feign.FeignException;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileManagementClient fileManagementClient;

    @Autowired
    private UserManagementClient userManagementClient;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void register(UserRegisterRequest userInfoRequest) {
        try {
            UserEntity user = new UserEntity();
            user.setUsername(userInfoRequest.getUsername());
            user.setPassword(passwordEncoder.encode(userInfoRequest.getPassword()));
            user = userRepository.save(user);
            fileManagementClient.createRoot(user.getId());
            userManagementClient.creteUser(user.getId(), userRequest(userInfoRequest));
        } catch (FeignException.Forbidden e) {
            throw new RuntimeException("Root not created");
        }
    }

    private UserRequest userRequest(UserRegisterRequest userInfoRequest) {
        UserRequest userRequest = new UserRequest();
        userRequest.setEmail(userInfoRequest.getEmail());
        userRequest.setCreatedDate(new Date());
        userRequest.setFirstName(userInfoRequest.getFirstName());
        userRequest.setLastName(userInfoRequest.getLastName());
        userRequest.setLastModifiedDate(new Date());
        return userRequest;
    }

    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public UserEntity loadUserByUsername(String username) {
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new NotFoundException("User not found with username: " + username);
        }
        return  userOptional.get();
    }

    public boolean checkCredentials(String username, String password) {
        Optional<UserEntity> user = findByUsername(username);
        return user.isPresent() && user.get().getPassword() != null && passwordEncoder.matches(password, user.get().getPassword());
    }

    public UserEntity getUserByUsername(String username) {
        return userRepository.getByUsername(username).orElseThrow(() -> new NotFoundException("User not found"));
    }

    public void changePassword(String username, String password) {
        UserEntity user = findByUsername(username).orElseThrow(() -> new NotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    public UserEntity getUserName(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    public void deleteByUserId(UUID userId) {
        userRepository.deleteById(userId);
    }
}
