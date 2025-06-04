package com.example.websitebackend.content;

import com.example.websitebackend.content.daos.ContentData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ContentService {

    private static final Logger log = LoggerFactory.getLogger(ContentService.class);

    private final ContentProperties contentProperties;
    private final ObjectMapper objectMapper;
    private final Path contentPath;

    private ContentData contentData;

    public ContentService(ContentProperties contentProperties, ObjectMapper objectMapper) throws IOException {
        this.contentProperties = contentProperties;
        this.objectMapper = objectMapper;
        if (contentProperties.path() == null || contentProperties.path().isBlank()) {
            throw new IllegalStateException("Content path must be set.");
        }
        this.contentPath = Path.of(contentProperties.path());
        Files.createDirectories(contentPath);
        log.info("Content path initialized at: {}", contentPath.toAbsolutePath());
    }

    public ContentData getContent() {
        return contentData;
    }

    public void setContent(MultipartFile contentFile) {
        Path tempDir = null;

        log.info("Setting content from file: {}", contentFile.getOriginalFilename());
        try (var is = new ZipInputStream(contentFile.getInputStream())) {
            tempDir = Files.createTempDirectory("content");
            log.info("Temporary directory created at: {}", tempDir.toAbsolutePath());

            ZipEntry entry;
            while ((entry = is.getNextEntry()) != null) {
                var name = entry.getName();
                log.info("Processing ZIP entry: {}", name);
                if (name.equals(contentProperties.jsonFileName())) {
                    var contentDataJson = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    contentData = objectMapper.readValue(contentDataJson, ContentData.class);
                } else {
                    var filePath = tempDir.resolve(name);
                    if (entry.isDirectory()) {
                        log.info("Creating directory: {}", filePath.toAbsolutePath());
                        Files.createDirectories(tempDir.resolve(name));
                    } else {
                        log.info("Extracting file to: {}", filePath.toAbsolutePath());
                        Files.copy(is, filePath);
                    }
                }
            }

            if (contentData == null) {
                log.error("ZIP file does not contain content.json.");
                throw new IllegalArgumentException("ZIP file must contain content.json.");
            }

            copyFilesToContentFolder(tempDir);

        } catch (Exception e) {
            log.error("Failed to process ZIP file: {}", contentFile.getOriginalFilename(), e);
            // clean up any temporary files created
            if (tempDir != null && Files.exists(tempDir)) {
                cleanUpTempDirectory(tempDir);
            }

            throw new IllegalArgumentException("Failed to read ZIP file.", e);
        }
    }

    private void copyFilesToContentFolder(Path tempDir) throws IOException {
        Files.walkFileTree(tempDir, new SimpleFileVisitor<>() {
            @Override
            @Nonnull
            public FileVisitResult visitFile(@Nonnull Path file, @Nonnull BasicFileAttributes attrs) throws IOException {
                log.info("Moving file: {} to {}", file.toAbsolutePath(), contentPath.resolve(tempDir.relativize(file)));
                Files.createDirectories(contentPath.resolve(tempDir.relativize(file.getParent())));
                Files.move(file, contentPath.resolve(tempDir.relativize(file)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            @Nonnull
            public FileVisitResult postVisitDirectory(@Nonnull Path dir, IOException exc) throws IOException {
                log.info("Deleting directory: {}", dir.toAbsolutePath());
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void cleanUpTempDirectory(Path tempDir) {
        try {
            Files.walkFileTree(tempDir, new SimpleFileVisitor<>() {
                @Override
                @Nonnull
                public FileVisitResult visitFile(@Nonnull Path file, @Nonnull BasicFileAttributes attrs) throws IOException {
                    log.info("Deleting temporary file: {}", file.toAbsolutePath());
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                @Nonnull
                public FileVisitResult postVisitDirectory(@Nonnull Path dir, IOException exc) throws IOException {
                    log.info("Deleting temporary directory: {}", dir.toAbsolutePath());
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("Failed to clean up temporary directory: {}", tempDir.toAbsolutePath(), e);
            throw new RuntimeException(e);
        }
    }
}
