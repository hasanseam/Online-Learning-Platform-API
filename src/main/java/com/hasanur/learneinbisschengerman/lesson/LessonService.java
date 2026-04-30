package com.hasanur.learneinbisschengerman.lesson;

import com.hasanur.learneinbisschengerman.course.Course;
import com.hasanur.learneinbisschengerman.course.CourseService;
import com.hasanur.learneinbisschengerman.exceptions.ResourceNotFoundException;
import com.hasanur.learneinbisschengerman.lesson.Dtos.CreateLessonDto;
import com.hasanur.learneinbisschengerman.lesson.Dtos.LessonResponseDto;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class LessonService {
    public final LessonRepository lessonRepository;
    private final CourseService courseService;

    public LessonService(LessonRepository lessonRepository, CourseService courseService) {
        this.lessonRepository = lessonRepository;
        this.courseService = courseService;
    }

    public Lesson createLesson(Long courseId, CreateLessonDto createLessonDto) {

        Course course = courseService.getCourseOrThrow(courseId);

        Lesson lesson = new Lesson();
        lesson.setTitle(createLessonDto.title());
        lesson.setContentText(createLessonDto.contentText());
        lesson.setVideoKey(createLessonDto.videoKey());
        lesson.setOrderNumber(createLessonDto.orderNumber());

        lesson.setCourse(course);

        return this.lessonRepository.save(lesson);
    }

    public List<LessonResponseDto> getLessonsByCourse(Long courseId) {
        // 1️⃣ Check if course exists
        if (!courseService.isCourseAvailable(courseId)) {
            throw new ResourceNotFoundException("Course is not found");
        }

        // 2️⃣ Fetch lessons
        return lessonRepository.findByCourseIdOrderByOrderNumber(courseId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public LessonResponseDto getLessonById(Long courseId, Long lessonId) {
        Lesson lesson = lessonRepository.findByIdAndCourseId(lessonId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        return mapToDto(lesson);
    }

    public LessonResponseDto updateLesson(Long courseId, Long lessonId, CreateLessonDto dto) {
        Lesson lesson = lessonRepository.findByIdAndCourseId(lessonId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        lesson.setTitle(dto.title());
        lesson.setContentText(dto.contentText());
        lesson.setVideoKey(dto.videoKey());
        lesson.setOrderNumber(dto.orderNumber());

        lessonRepository.save(lesson);
        return mapToDto(lesson);
    }

    public void deleteLesson(Long courseId, Long lessonId) {
        Lesson lesson = lessonRepository.findByIdAndCourseId(lessonId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        lessonRepository.delete(lesson);
    }

    // This method is used to attach a video to a lesson by setting the video key.
    public void attachVideo(Long lessonId, String key) {

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        lesson.setVideoKey(key);

        lessonRepository.save(lesson);
    }

    public LessonResponseDto mapToDto(Lesson lesson) {
        return new LessonResponseDto(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getContentText(),
                lesson.getVideoKey(),
                lesson.getOrderNumber());
    }

}
