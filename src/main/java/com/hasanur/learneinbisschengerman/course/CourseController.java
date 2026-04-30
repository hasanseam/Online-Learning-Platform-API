package com.hasanur.learneinbisschengerman.course;

import com.hasanur.learneinbisschengerman.course.CourseService;
import com.hasanur.learneinbisschengerman.course.Dtos.CourseCreateDto;
import com.hasanur.learneinbisschengerman.course.Dtos.CourseResponseDto;
import com.hasanur.learneinbisschengerman.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // CREATE
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CourseResponseDto> createCourse(@Valid @RequestBody CourseCreateDto dto) {
        CourseResponseDto course = courseService.createCourse(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(course);
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<CourseResponseDto>> getAllCourses() {
        List<CourseResponseDto> courses = courseService.getCourses();
        return ResponseEntity.ok(courses);
    }

    // READ ONE
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponseDto> getCourseById(@PathVariable Long courseId) {
        CourseResponseDto course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        return ResponseEntity.ok(course);
    }

    // UPDATE
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{courseId}")
    public ResponseEntity<CourseResponseDto> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseCreateDto dto) {
        CourseResponseDto updated = courseService.updateCourse(courseId, dto);
        return ResponseEntity.ok(updated);
    }

    // DELETE
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

}
