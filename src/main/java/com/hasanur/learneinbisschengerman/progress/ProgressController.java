package com.hasanur.learneinbisschengerman.progress;

import com.hasanur.learneinbisschengerman.user.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @PostMapping("/complete")
    public ResponseEntity<?> markCompleted(
            @RequestParam Long courseId,
            @RequestParam Long lessonId,
            Authentication auth) {
        
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Long userId = userDetails.getId();

        // Fixed parameter order: userId, lessonId, courseId
        progressService.markLessonCompleted(userId, lessonId, courseId);

        return ResponseEntity.ok(Map.of("message", "Lesson completed"));
    }

}
