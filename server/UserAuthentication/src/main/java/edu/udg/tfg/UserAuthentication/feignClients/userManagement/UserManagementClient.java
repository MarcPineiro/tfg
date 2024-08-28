package edu.udg.tfg.UserAuthentication.feignClients.userManagement;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Component
@FeignClient(name = "${user.management.microservice.url}")
public interface UserManagementClient {

    @PostMapping("/users")
    void creteUser(@RequestHeader("X-User-Id") UUID userId, @RequestBody UserRequest userRequest);
}
