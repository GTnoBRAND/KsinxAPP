package org.jas.ksinxapp.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(){
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

        try{
            //create directory if not exists
            Files.createDirectories(this.fileStorageLocation);
        }catch(Exception e){
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.",e);
        }

    }

    public String storeFile(MultipartFile file){
        //clean the file name
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        // Add a timestamp to make the filename completely unique
        String fileName = System.currentTimeMillis() + "_" + originalFileName;

        try{
            //check if the file name contains invalid characters
            if(fileName.contains("..")){
                throw new RuntimeException("Cannot store file outside current directory");
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return the local URL that the frontend can use
            return "http://localhost:8080/uploads/" + fileName;


        }catch(IOException ex){
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

}
