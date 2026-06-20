package org.jas.ksinxapp.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinIoStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-public}")
    private String publicBucket;
    @Value("${minio.bucket-private}")
    private String privateBucket;
    @Value("${minio.public-url}")
    private String publicUrl;
    @Value("${minio.endpoint}")
    private String endpoint;


    //build a unique object key from original filename
    public String buildKey(MultipartFile file){
        String original = StringUtils.cleanPath(
                Objects.requireNonNull(file.getOriginalFilename()));
        if(original.contains("..")){
            throw new RuntimeException("Invalid file path: " + original);
        }
        //prevent from collide
        return System.currentTimeMillis() + "_" + original;
    }

    //1 public upload returns a permanent url
    public String publicUpload(MultipartFile file){
        String key = buildKey(file);
        try{
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(publicBucket)
                            .object(key)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //public bucket anonymous downloads -> permanent url works
        return publicUrl + "/" + key;
    }

    //2 private upload
    public String privateUpload(MultipartFile file)
    {
        String key = buildKey(file);
        try{
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(privateBucket)
                            .object(key)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return key;
    }

    //3 generate temporary pre-signed url for private file
    public String generatePreSignedUrl(String objectKey){
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(privateBucket)
                            .object(objectKey)
                            .expiry(5, TimeUnit.MINUTES)
                            .build()
            );
            return url.replace(endpoint, publicUrl);
        }catch (Exception e){
            throw new RuntimeException("Failed to generate pre-signed key!",e);
        }
    }
}
