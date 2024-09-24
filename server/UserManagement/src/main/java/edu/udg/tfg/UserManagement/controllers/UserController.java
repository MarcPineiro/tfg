package edu.udg.tfg.UserManagement.controllers;

import edu.udg.tfg.UserManagement.controllers.requests.UserRequest;
import edu.udg.tfg.UserManagement.entities.UserInfo;
import edu.udg.tfg.UserManagement.services.UserService;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("")
    public ResponseEntity<?> registerUser(@RequestHeader("X-User-Id") UUID userId, @RequestBody UserRequest userRequest) {
        UserInfo userInfo = createUser(userRequest);
        userInfo.setId(userId);

        UserInfo newUserInfo = userService.createUser(userInfo);
        return ResponseEntity.ok(newUserInfo);
    }

    private UserInfo createUser(UserRequest userRequest) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userRequest.getId());
        userInfo.setEmail(userRequest.getEmail());
        userInfo.setLastName(userRequest.getLastName());
        userInfo.setCreatedDate(userRequest.getCreatedDate());
        userInfo.setLastModifiedDate(userRequest.getLastModifiedDate());
        userInfo.setFirstName(userRequest.getFirstName());

        return userInfo;
    }

    @GetMapping("")
    public ResponseEntity<?> getUser(@RequestHeader("X-User-Id") UUID id) {
        UserInfo newUserInfo = userService.getUserById(id);
        return ResponseEntity.ok(newUserInfo);
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteUser(@RequestHeader("X-User-Id") UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("")
    public ResponseEntity<?> updateUser(@RequestHeader("X-User-Id") UUID id, @RequestBody UserRequest userRequest) {
        UserInfo updatedUserInfo = userService.updateUser(id, userRequest);
        return ResponseEntity.ok(updatedUserInfo);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleRuntimeException(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Element not found");
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<String> handleForbiddenException(ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this resource");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
}