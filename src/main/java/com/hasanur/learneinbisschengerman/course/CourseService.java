package com.hasanur.learneinbisschengerman.course;

import com.hasanur.learneinbisschengerman.course.Dtos.CourseCreateDto;
import com.hasanur.learneinbisschengerman.course.Dtos.CourseResponseDto;
import com.hasanur.learneinbisschengerman.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public CourseResponseDto createCourse(CourseCreateDto courseCreateDto) {

        Course course = new Course();
        course.setTitle(courseCreateDto.title());
        course.setDescription(courseCreateDto.description());
        course.setLevel(courseCreateDto.level());

        courseRepository.save(course);
        return mapToDto(course);
    }

    public List<CourseResponseDto> getCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public Optional<CourseResponseDto> getCourseById(Long courseId) {
        return courseRepository.findById(courseId).map(this::mapToDto);
    }

    public boolean isCourseAvailable(Long courseId) {
        return courseRepository.existsById(courseId);
    }

    public Course getCourseOrThrow(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    public CourseResponseDto updateCourse(Long courseId, CourseCreateDto dto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        course.setTitle(dto.title());
        course.setDescription(dto.description());
        course.setLevel(dto.level());

        courseRepository.save(course);
        return mapToDto(course);
    }

    // DELETE
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        courseRepository.delete(course);
    }

    private CourseResponseDto mapToDto(Course course) {
        return new CourseResponseDto(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getLevel());
    }
}
