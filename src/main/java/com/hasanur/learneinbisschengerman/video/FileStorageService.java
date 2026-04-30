package com.hasanur.learneinbisschengerman.video;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class FileStorageService {
    private final S3Client s3Client;

    @Value("${r2.bucket}")
    private String bucket;

    public FileStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadVideo(MultipartFile file, Long lessonId) throws IOException {

        // Create unique file name
        String key = "videos/" + lessonId + "/" + UUID.randomUUID() + ".mp4";

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return key;
    }
}
