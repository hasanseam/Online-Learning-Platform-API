package com.hasanur.learneinbisschengerman.video;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import com.hasanur.learneinbisschengerman.lesson.Lesson;
import com.hasanur.learneinbisschengerman.lesson.LessonRepository;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class VideoStreamService {
    private final LessonRepository lessonRepository;
    private final S3Client s3Client;

    @Value("${r2.bucket}")
    private String bucket;

    public VideoStreamService(LessonRepository lessonRepository, S3Client s3Client) {
        this.lessonRepository = lessonRepository;
        this.s3Client = s3Client;
    }

    public ResponseEntity<Resource> getPlaylist(Long courseId, Long lessonId) {
        validate(courseId, lessonId);

        return stream("videos/" + lessonId + "/index.m3u8",
                "application/vnd.apple.mpegurl");
    }

    public ResponseEntity<Resource> getSegment(Long courseId, Long lessonId, String fileName) {
        validate(courseId, lessonId);

        return stream("videos/" + lessonId + "/" + fileName,
                "video/mp2t");
    }

    private void validate(Long courseId, Long lessonId) {
        Lesson lesson = lessonRepository
                .findByIdAndCourseId(lessonId, courseId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        if (!"READY".equals(lesson.getVideoStatus())) {
            throw new RuntimeException("Video not ready");
        }
    }

    private ResponseEntity<Resource> stream(String key, String contentType) {

        ResponseInputStream<GetObjectResponse> object = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(new InputStreamResource(object));
    }

}