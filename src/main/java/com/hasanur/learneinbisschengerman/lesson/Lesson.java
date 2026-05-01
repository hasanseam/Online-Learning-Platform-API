package com.hasanur.learneinbisschengerman.lesson;

import com.hasanur.learneinbisschengerman.course.Course;
import jakarta.persistence.*;

@Entity
@Table(name = "lesson")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String contentText;

    @Column(nullable = true)
    private String videoKey;

    @Column
    private String videoStatus; // PROCESSING, READY, FAILED

    @Column(nullable = false)
    private Integer orderNumber;

    public Lesson(Long id, Course course, String title, String contentText, String videoKey, String videoStatus, Integer orderNumber) {
        this.id = id;
        this.course = course;
        this.title = title;
        this.contentText = contentText;
        this.videoKey = videoKey;
        this.orderNumber = orderNumber;
        this.videoStatus = videoStatus;
    }

    public Lesson() {

    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public String getVideoKey() {
        return videoKey;
    }

    public void setVideoKey(String videoKey) {
        this.videoKey = videoKey;
    }

    public String getVideoStatus(){return videoStatus;}
    public void setVideoStatus(String videoStatus){this.videoStatus=videoStatus;}

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

}
