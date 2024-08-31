package edu.udg.tfg.UserManagement.services;

import edu.udg.tfg.UserManagement.controllers.requests.UserRequest;
import edu.udg.tfg.UserManagement.entities.UserInfo;
import edu.udg.tfg.UserManagement.queue.Receiver;
import edu.udg.tfg.UserManagement.queue.Sender;
import edu.udg.tfg.UserManagement.repositories.UserRepository;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Sender sender;

    public List<UserInfo> getAllUsers() {
        return userRepository.findAll();
    }

    public UserInfo getUserById(UUID id) {
        return userRepository.findById(id)
                              .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public UserInfo createUser(UserInfo userInfo) {
        return userRepository.save(userInfo);
    }

    public UserInfo updateUser(UUID id, UserRequest userRequest) {
        UserInfo userInfo = getUserById(id);
        userInfo.setFirstName(userRequest.getFirstName());
        userInfo.setLastName(userRequest.getLastName());
        userInfo.setEmail(userRequest.getEmail());

        return userRepository.save(userInfo);
    }

    public void deleteUser(UUID id) {
        sender.deleteUser(id);
        userRepository.deleteById(id);
    }
}