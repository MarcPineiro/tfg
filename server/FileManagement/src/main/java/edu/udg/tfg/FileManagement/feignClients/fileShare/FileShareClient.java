package edu.udg.tfg.FileManagement.feignClients.fileShare;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Component
@FeignClient(name = "${file.share.microservice.url}")
public interface FileShareClient {

    @PostMapping("/{userId}")
    void shareFile(@PathVariable("userId") String userId, @RequestBody SharedRequest sharedRequest);

    @GetMapping("/{userId}")
    FilesSharedResponse getSharedFiles(@PathVariable("userId") String userId);

    @DeleteMapping("/{fileId}/{userId}")
    void revokeSharedFile(@PathVariable("fileId") String fileId, @PathVariable("userId") String userId);

    @GetMapping("/user/{elementId}")
    UsersSharedResponse getUsersShared(@PathVariable("elementId") UUID elementId);
}
