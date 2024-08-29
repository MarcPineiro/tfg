package edu.udg.tfg.FileManagement.utils;

import edu.udg.tfg.FileManagement.entities.FileEntity;
import edu.udg.tfg.FileManagement.entities.FolderEntity;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.zip.*;

public class ZipUtil {

    public static Resource createZipFromFolder(FolderEntity rootFolder, String zipFilePath, FileUtil fileUtil) {
        try {
            FileOutputStream fos = new FileOutputStream(zipFilePath);
            ZipOutputStream zos = new ZipOutputStream(fos);
            addToZip(rootFolder, "", zos, fileUtil);
            zos.close();
            fos.close();
            return new FileSystemResource(zipFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void addToZip(FolderEntity folder, String parentPath, ZipOutputStream zos, FileUtil fileUtil) throws IOException {
        String folderPath = parentPath + folder.getName() + "/";
        if(folder.getFiles().isEmpty())
        {
            //Add an empty folder to the zip
            ZipEntry zipEntry = new ZipEntry(folderPath);
            zos.putNextEntry(zipEntry);
            zos.closeEntry();
        }
        else {
            for (FileEntity file : folder.getFiles()) {
                // Retrieve file data using FileUtil
                byte[] fileData = fileUtil.getFileDataById(file.getElementId());
                // Add file to zip
                ZipEntry zipEntry = new ZipEntry(folderPath + file.getName());
                zos.putNextEntry(zipEntry);
                zos.write(fileData);
                zos.closeEntry();
            }
        }
        for (FolderEntity subFolder : folder.getChildren()) {
            addToZip(subFolder, folderPath, zos, fileUtil);
        }
    }
}
