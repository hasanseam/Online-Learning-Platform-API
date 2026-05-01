package com.hasanur.learneinbisschengerman.video;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.Resource;

@RestController
@RequestMapping("/courses/{courseId}/lessons/{lessonId}/video")
public class VideoStreamController {

    private final VideoStreamService service;

    public VideoStreamController(VideoStreamService service) {
        this.service = service;
    }

    @GetMapping("/playlist")
    public ResponseEntity<Resource> playlist(
            @PathVariable Long courseId,
            @PathVariable Long lessonId) {
        return service.getPlaylist(courseId, lessonId);
    }

    @GetMapping("/segment/{fileName:.+}")
    public ResponseEntity<Resource> segment(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @PathVariable String fileName) {
        return service.getSegment(courseId, lessonId, fileName);
    }
}
