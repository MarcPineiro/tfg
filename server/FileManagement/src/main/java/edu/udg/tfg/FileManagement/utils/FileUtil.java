package edu.udg.tfg.FileManagement.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FileUtil {

    @Value("${file.storage.path}")
    private String storagePath;

    public void storeFile(UUID fileId, MultipartFile file) throws IOException {
        String fileName = fileId.toString(); // Rename file to its ID
        Path filePath = load(fileName);
        Files.copy(file.getInputStream(), filePath);
    }

    public byte[] getFileDataById(UUID fileId) throws IOException {
        String fileName = fileId.toString();
        Path filePath = load(fileName);
        return Files.readAllBytes(filePath);
    }

    public Path load(String fileName) {
        return Paths.get(storagePath, fileName);
    }

    public Resource loadAsResource(UUID fileId) {
        try {
            Path file = load(fileId.toString());
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new RuntimeException("Could not read file: " + fileId); //StorageFileNotFoundException("Could not read file: " + filename);

            }
        }
        catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + fileId, e); //StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    /*public void store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            Path destinationFile = this.rootLocation.resolve(
                            Paths.get(file.getOriginalFilename()))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }*/
}
