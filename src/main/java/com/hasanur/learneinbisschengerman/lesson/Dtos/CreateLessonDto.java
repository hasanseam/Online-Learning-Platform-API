package com.hasanur.learneinbisschengerman.lesson.Dtos;

public record CreateLessonDto(
                String title,
                String contentText,
                String videoKey,
                Integer orderNumber) {
}
