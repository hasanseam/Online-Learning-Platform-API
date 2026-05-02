package com.hasanur.learneinbisschengerman.lesson;

import com.hasanur.learneinbisschengerman.lesson.Dtos.CreateLessonDto;
import com.hasanur.learneinbisschengerman.lesson.Dtos.LessonResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses/{courseId}/lessons")
public class LessonController {

        private final LessonService lessonService;

        public LessonController(LessonService lessonService) {
                this.lessonService = lessonService;
        }

        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping
        public ResponseEntity<LessonResponseDto> createLesson(@PathVariable Long courseId,
                        @RequestBody CreateLessonDto createLessonDto) {
                return ResponseEntity.status(HttpStatus.CREATED).body(
                                lessonService.createLesson(courseId, createLessonDto));
        }

        @GetMapping
        public ResponseEntity<List<LessonResponseDto>> getLessons(
                        @PathVariable Long courseId) {
                return ResponseEntity.ok(
                                lessonService.getLessonsByCourse(courseId));
        }

        @GetMapping("/{lessonId}")
        public ResponseEntity<LessonResponseDto> getLesson(
                        @PathVariable Long courseId,
                        @PathVariable Long lessonId) {
                return ResponseEntity.ok(
                                lessonService.getLessonDtoById(courseId, lessonId));
        }

        @PreAuthorize("hasRole('ADMIN')")
        @PutMapping("/{lessonId}")
        public ResponseEntity<LessonResponseDto> updateLesson(
                        @PathVariable Long courseId,
                        @PathVariable Long lessonId,
                        @Valid @RequestBody CreateLessonDto dto) {
                return ResponseEntity.ok(
                                lessonService.updateLesson(courseId, lessonId, dto));
        }

        @PreAuthorize("hasRole('ADMIN')")
        @DeleteMapping("/{lessonId}")
        public ResponseEntity<Void> deleteLesson(
                        @PathVariable Long courseId,
                        @PathVariable Long lessonId) {
                lessonService.deleteLesson(courseId, lessonId);
                return ResponseEntity.noContent().build();
        }
}
