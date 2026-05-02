package com.hasanur.learneinbisschengerman.video;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hasanur.learneinbisschengerman.exceptions.ResourceNotFoundException;
import com.hasanur.learneinbisschengerman.lesson.Lesson;
import com.hasanur.learneinbisschengerman.lesson.LessonRepository;

import java.io.IOException;

@Service
public class VideoService {
    private final LessonRepository lessonRepository;
    private final FileStorageService fileStorageService;

    public VideoService(LessonRepository lessonRepository, FileStorageService fileStorageService) {
        this.lessonRepository = lessonRepository;
        this.fileStorageService = fileStorageService;
    }

    public String uploadAndAttachVideo(Long lessonId, Long courseId, MultipartFile file)
            throws IOException, IOException {
        Lesson lesson = lessonRepository
                .findByIdAndCourseId(lessonId, courseId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        String key = fileStorageService.uploadVideo(file, lessonId);
        lesson.setVideoKey(key);
        lessonRepository.save(lesson);
        return key;
    }

    public void updateVideoStatus(Long lessonId, String status) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        lesson.setVideoStatus(status);
        lessonRepository.save(lesson);
    }

    public void attachVideo(Long lessonId, String videoKey) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        lesson.setVideoKey(videoKey);
        lessonRepository.save(lesson);
    }

    public String getVideoStatus(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        return lesson.getVideoStatus();
    }

}
