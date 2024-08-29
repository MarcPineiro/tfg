package edu.udg.tfg.UserAuthentication.controllers;

import edu.udg.tfg.UserAuthentication.controllers.requests.ChangePasswordRequest;
import edu.udg.tfg.UserAuthentication.controllers.requests.UserInfoRequest;
import edu.udg.tfg.UserAuthentication.controllers.requests.UserRegisterRequest;
import edu.udg.tfg.UserAuthentication.entities.UserEntity;
import edu.udg.tfg.UserAuthentication.services.UserService;
import edu.udg.tfg.UserAuthentication.utils.JwtTokenUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.hc.core5.http.HttpStatus;

import java.util.UUID;

@RestController
@RequestMapping("/users/auth")
public class AuthenticationController {

    public static final Logger LOG = LoggerFactory.getLogger(AuthenticationController.class);

    public static final String ACCESS_NAME = "accessToken";
    public static final String REFRESH_NAME = "refreshToken";

    private final UserService userService;

    private final JwtTokenUtil jwtTokenUtil;

    public AuthenticationController(@Qualifier("userService") UserService userService, JwtTokenUtil jwtTokenUtil) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    private ResponseEntity<?> addTokenHeaders(String username) {
        // Generate tokens
        String accessToken = jwtTokenUtil.generateToken(username, false);
        String refreshToken = jwtTokenUtil.generateToken(username, true);

        // Add tokens to the response headers
        return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).header("X-Refresh-Token", refreshToken).build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterRequest userInfoRequest, HttpServletResponse response) {
        LOG.info("Registering new user: {}", userInfoRequest);
        if (userService.findByUsername(userInfoRequest.getUsername()).isPresent()) {
            LOG.info("User not registered. Username already taken.");
            return ResponseEntity.badRequest().body("Username is already taken");
        }
        userService.register(userInfoRequest);
        LOG.info("User registered successfully");


        return addTokenHeaders(userInfoRequest.getUsername());
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserInfoRequest userInfoRequest) {
        LOG.info("Authenticating user: {}", userInfoRequest);

        if(!userService.checkCredentials(userInfoRequest.getUsername(), userInfoRequest.getPassword())) {
            LOG.info("User or password not valid");
            return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).body("Invalid credentials");
        }
        LOG.info("User authenticated successfully");


        return addTokenHeaders(userInfoRequest.getUsername());
    }

    @PostMapping("/keep-alive")
    public ResponseEntity<?> keepAlive(@RequestHeader("X-Refresh-Token") String refreshToken, HttpServletResponse response) {
        LOG.info("Refreshing tokens using refresh token from header.");

        if (!jwtTokenUtil.validateTokenExpiration(refreshToken)) {
            return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).body("Invalid or expired refresh token");
        }

        String username = jwtTokenUtil.extractUsername(refreshToken);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).body("Invalid refresh token");
        }

        return addTokenHeaders(username);
    }

    @PostMapping("/check")
    public ResponseEntity<?> authenticateUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        LOG.info("Checking if token is valid: {}", token);

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).body("Token is not valid");
        }

        String jwtToken = token.substring(7);
        if (!jwtTokenUtil.validateTokenExpiration(jwtToken)) {
            return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).body("Token is not valid");
        }

        String username = jwtTokenUtil.extractUsername(jwtToken);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).body("Invalid token");
        }

        UserEntity user = userService.findByUsername(username).orElseThrow(() -> new NotFoundException("User not found"));
        LOG.info("User authenticated successfully");

        return ResponseEntity.ok(user.getId());
    }

    @GetMapping("/{username}/id")
    public ResponseEntity<?> getId(@PathVariable("username") String username) {
        UserEntity userInfo = userService.getUserByUsername(username);
        return ResponseEntity.ok(userInfo.getId());
    }

    @GetMapping("/username")
    public ResponseEntity<?> getUserName(@RequestHeader("X-User-Id") UUID id) {
        UserEntity userInfo = userService.getUserName(id);
        return ResponseEntity.ok(userInfo.getUsername());
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader(HttpHeaders.AUTHORIZATION) String token, @RequestBody ChangePasswordRequest request) {
        LOG.info("Changing password for user");

        String jwtToken = token.substring(7);
        if (!jwtTokenUtil.validateTokenExpiration(jwtToken)) {
            return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).body("Token is not valid");
        }

        String username = jwtTokenUtil.extractUsername(jwtToken);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).body("Invalid token");
        }

        userService.changePassword(username, request.getPassword());
        return ResponseEntity.ok("Password changed successfully");
    }

    @RequestMapping(value = "/login", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handlePreflight() {
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleRuntimeException(NotFoundException e) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND).body("Element not found");
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<String> handleForbiddenException(ForbiddenException e) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body("User does not have access to this resource");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
}
