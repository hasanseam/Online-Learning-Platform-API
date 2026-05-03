package com.hasanur.learneinbisschengerman.progress;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Entity
@Table(name = "lesson_completion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonCompletion {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long lessonId;
    private Long courseId;
    private boolean completed;
    private LocalDateTime completedAt;
}
