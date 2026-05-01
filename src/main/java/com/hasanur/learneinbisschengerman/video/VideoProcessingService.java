package com.hasanur.learneinbisschengerman.video;

import com.hasanur.learneinbisschengerman.lesson.Lesson;
import com.hasanur.learneinbisschengerman.lesson.LessonRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;

@Service
public class VideoProcessingService {
    private final S3Client s3Client;
    private final LessonRepository lessonRepository;

    @Value("${r2.bucket}")
    private String bucket;

    public VideoProcessingService(S3Client s3Client, LessonRepository lessonRepository) {
        this.s3Client = s3Client;
        this.lessonRepository = lessonRepository;
    }

    @Async
    public void processAsync(MultipartFile file, Lesson lesson) {

        Path tempFile = null;
        Path outputDir = null;

        try {
            System.out.println("🔥 Processing lesson: " + lesson.getId());

            // 1. Save upload safely
            tempFile = Files.createTempFile("upload-", ".mp4");
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // 2. Create output dir
            outputDir = Files.createTempDirectory("hls-" + lesson.getId());

            String playlistPath = outputDir.resolve("index.m3u8").toString();
            String segmentPattern = outputDir.resolve("segment_%03d.ts").toString();

            // 3. FFmpeg
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", tempFile.toString(),
                    "-c:v", "libx264",
                    "-c:a", "aac",
                    "-preset", "fast",
                    "-g", "48",
                    "-sc_threshold", "0",
                    "-hls_time", "10",
                    "-hls_list_size", "0",
                    "-hls_segment_filename", segmentPattern,
                    "-f", "hls",
                    playlistPath
            );

            pb.redirectErrorStream(true);

            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("FFMPEG: " + line);
            }

            int exitCode = process.waitFor();
            System.out.println("✅ FFmpeg finished: " + exitCode);

            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg failed");
            }

            // 4. Rewrite playlist (CRITICAL)
            Path playlistFile = outputDir.resolve("index.m3u8");

            List<String> lines = Files.readAllLines(playlistFile);

            List<String> updated = lines.stream()
                    .map(l -> {
                        if (l.endsWith(".ts")) {
                            return "/courses/" + lesson.getCourse().getId()
                                    + "/lessons/" + lesson.getId()
                                    + "/video/segment/" + l;
                        }
                        return l;
                    })
                    .toList();

            Files.write(playlistFile, updated);

            // 5. Upload all files to R2
            Files.list(outputDir)
                    .filter(p -> !Files.isDirectory(p))
                    .forEach(p -> {
                        try {
                            String key = "videos/" + lesson.getId() + "/" + p.getFileName();

                            s3Client.putObject(
                                    PutObjectRequest.builder()
                                            .bucket(bucket)
                                            .key(key)
                                            .contentType(getContentType(p))
                                            .build(),
                                    RequestBody.fromFile(p)
                            );

                        } catch (Exception e) {
                            throw new RuntimeException("Upload failed: " + e.getMessage());
                        }
                    });

            // 6. Update DB
            lesson.setVideoKey("videos/" + lesson.getId() + "/index.m3u8");
            lesson.setVideoStatus("READY");

        } catch (Exception e) {
            e.printStackTrace();
            lesson.setVideoStatus("FAILED");

        } finally {
            lessonRepository.save(lesson);

            // cleanup
            try {
                if (tempFile != null) Files.deleteIfExists(tempFile);

                if (outputDir != null) {
                    Files.walk(outputDir)
                            .sorted(Comparator.reverseOrder())
                            .forEach(p -> {
                                try { Files.delete(p); } catch (Exception ignored) {}
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
