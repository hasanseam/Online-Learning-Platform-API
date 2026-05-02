package com.hasanur.learneinbisschengerman.video;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hasanur.learneinbisschengerman.lesson.Lesson;
import com.hasanur.learneinbisschengerman.lesson.LessonService;

@RestController
@RequestMapping("/courses/{courseId}/lessons/{lessonId}/video")
public class VideoController {

        private final VideoProcessingService videoProcessingService;
        private final LessonService lessonService;
        private final VideoService videoService;

        public VideoController(VideoProcessingService videoProcessingService, VideoService videoService,
                        LessonService lessonService) {
                this.videoProcessingService = videoProcessingService;
                this.lessonService = lessonService;
                this.videoService = videoService;
        }

        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping
        public ResponseEntity<?> uploadVideo(
                        @PathVariable Long courseId,
                        @PathVariable Long lessonId,
                        @RequestParam("file") MultipartFile file) throws IOException {

                videoService.updateVideoStatus(lessonId, "PROCESSING");

                Lesson lesson = lessonService.getLessonEntityById(courseId, lessonId);

                videoProcessingService.processAsync(file, lesson);

                return ResponseEntity.ok(Map.of(
                                "message", "Upload started",
                                "status", "PROCESSING"));
        }

        @GetMapping("/status")
        public ResponseEntity<Map<String, String>> getStatus(
                        @PathVariable Long courseId,
                        @PathVariable Long lessonId) {
                return ResponseEntity.ok(Map.of(
                                "status", videoService.getVideoStatus(lessonId)));
        }

        /*** Legacy upload mp4 in R2 storage */
        /**
         * @PreAuthorize("hasRole('ADMIN')")
         * 
         * @PostMapping
         *              public ResponseEntity<Map<String, String>> uploadVideo(
         * @PathVariable Long courseId,
         * @PathVariable Long lessonId,
         *               @RequestParam("file") MultipartFile file) throws IOException {
         * 
         *               String videoKey = videoService.uploadAndAttachVideo(
         *               lessonId,
         *               courseId,
         *               file);
         * 
         *               return ResponseEntity.ok(Map.of(
         *               "message", "Video uploaded successfully",
         *               "videoKey", videoKey));
         *               }
         **/
}
