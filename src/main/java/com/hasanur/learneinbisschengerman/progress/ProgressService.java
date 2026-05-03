package com.hasanur.learneinbisschengerman.progress;

import com.hasanur.learneinbisschengerman.lesson.LessonRepository;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
public class ProgressService {
    private final LessonCompletionRepository lessonCompletionRepository;
    private final LessonRepository lessonRepository;

    public ProgressService(LessonCompletionRepository lessonCompletionRepository, LessonRepository lessonRepository) {
        this.lessonCompletionRepository = lessonCompletionRepository;
        this.lessonRepository = lessonRepository;
    }

    public void markLessonCompleted(Long userId, Long lessonId, Long courseId) {
        LessonCompletion lessonCompletion = lessonCompletionRepository
                .findByUserIdAndLessonId(userId, lessonId)
                .orElseGet(() -> {
                    LessonCompletion lc = new LessonCompletion();
                    lc.setUserId(userId);
                    lc.setLessonId(lessonId);
                    lc.setCourseId(courseId);
                    return lc;
                });

        lessonCompletion.setCompleted(true);
        lessonCompletion.setCompletedAt(LocalDateTime.now());

        lessonCompletionRepository.save(lessonCompletion);
    }

    public double getCourseProgress(Long userId, Long courseId) {

        long totalLessons = lessonRepository
                .findByCourseIdOrderByOrderNumber(courseId)
                .size();

        long completed = lessonCompletionRepository
                .countByUserIdAndCourseIdAndCompletedTrue(userId, courseId);

        if (totalLessons == 0) return 0;

        return (double) completed / totalLessons;
    }


}
