package com.hasanur.learneinbisschengerman.lesson;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class VideoProcessingService {
    private final S3Client s3Client;
    private final LessonRepository lessonRepository;

    @Value("${r2.bucket}")
    private String bucket;

    public VideoProcessingService(S3Client s3Client, LessonRepository lessonRepository) {
        this.s3Client = s3Client;
        this.lessonRepository = lessonRepository;
    }

    public void processAsync(MultipartFile file, Lesson lesson) {
        Path tempFile = null;
        Path outputDir = null;

        try {

            tempFile = Files.createTempFile("upload-", ".mp4");
            file.transferTo(tempFile.toFile());

            outputDir = Files.createTempDirectory("hls-" + lesson.getId());

            String outputPath = outputDir.resolve("index.m3u8").toString();

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", tempFile.toString(),
                    "-codec:", "copy",
                    "-start_number", "0",
                    "-hls_time", "10",
                    "-hls_list_size", "0",
                    "-f", "hls",
                    outputPath);

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg conversion failed");
            }

            Files.list(outputDir).forEach(path -> {
                try {
                    String key = "videos/" + lesson.getId() + "/" + path.getFileName();

                    s3Client.putObject(
                            PutObjectRequest.builder()
                                    .bucket(bucket)
                                    .key(key)
                                    .contentType(getContentType(path))
                                    .build(),
                            RequestBody.fromFile(path));

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            lesson.setVideoKey("videos/" + lesson.getId() + "/index.m3u8");
            lesson.setVideoStatus("READY");

        } catch (Exception e) {
            lesson.setVideoStatus("FAILED");
        } finally {
            lessonRepository.save(lesson);
            try {
                if (tempFile != null) Files.deleteIfExists(tempFile);
                if (outputDir != null) {
                    Files.walk(outputDir)
                            .sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try { Files.delete(path); } catch (Exception ignored) {}
                            });
                }
            } catch (Exception ignored) {}
        }
    }

    private String getContentType(Path path) {
        String name = path.getFileName().toString();
        if (name.endsWith(".m3u8"))
            return "application/vnd.apple.mpegurl";
        if (name.endsWith(".ts"))
            return "video/mp2t";
        return "application/octet-stream";
    }
}
