package com.example.websitebackend.content;

import com.example.websitebackend.content.daos.ContentData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class ContentService {

    private static final Logger log = LoggerFactory.getLogger(ContentService.class);

    private final ContentProperties contentProperties;
    private final ObjectMapper objectMapper;
    private final Path contentPath;

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

    public ContentData getContent(String lang) throws IOException {
        var file = contentPath.resolve(localizeFileName(contentProperties.jsonFileName(), lang));
        var resource = new FileSystemResource(file);
        if (!resource.exists() || !resource.isReadable()) {
            log.warn("Resource not found or not readable: {}", file);
            return null;
        }
        return objectMapper.readValue(resource.getInputStream(), ContentData.class);
    }

    private String localizeFileName(String fileName, String lang) {
        var fileParts = fileName.split("\\.");
        return fileParts[0] + "_" + lang + "." + fileParts[1];
    }

    public Resource getResource(String fileName) throws IOException {
        var file = contentPath.resolve("private/content/" + fileName);
        var resource = new FileSystemResource(file);

        if (!resource.exists() || !resource.isReadable()) {
            log.warn("Resource not found or not readable: {}", fileName);
            return null;
        }
        if (!file.startsWith(contentPath)) {
            log.warn("Resource is outside the content path: {}", fileName);
            return null;
        }

        log.info("Resource found: {}", fileName);
        log.info("Resource absolute path: {}", resource.getFile().getAbsolutePath());
        log.info("Resource length: {}", resource.contentLength());

        return resource;
    }

    public Resource getContentZip() {
        try (var baos = new ByteArrayOutputStream();
             var zos = new ZipOutputStream(baos)) {
            log.info("Creating in-memory content.zip from: {}", contentPath.toAbsolutePath());

            Files.walkFileTree(contentPath, new SimpleFileVisitor<>() {
                @Override
                @Nonnull
                public FileVisitResult visitFile(@Nonnull Path file, @Nonnull BasicFileAttributes attrs) throws IOException {
                    var relativeFileLocation = contentPath.relativize(file);
                    log.info("Adding file to ZIP: {}", relativeFileLocation);
                    var zipEntry = new ZipEntry(relativeFileLocation.toString().replace("\\", "/"));
                    zos.putNextEntry(zipEntry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });

            zos.finish();

            var zipBytes = baos.toByteArray();
            return new ByteArrayResource(zipBytes) {
                @Override
                public String getFilename() {
                    return "content.zip";
                }
            };

        } catch (IOException e) {
            log.error("Failed to create content.zip", e);
            throw new RuntimeException(e);
        }
    }

    /*
     * The zip file has the following structure:
     * - content_de.json
     * - content_en.json
     * - private/content/
     *     - <other files>
     */
    public void setContent(MultipartFile contentFile) {
        Path tempDir = null;

        log.info("Setting content from file: {}", contentFile.getOriginalFilename());
        try (var is = new ZipInputStream(contentFile.getInputStream())) {
            tempDir = Files.createTempDirectory("content");
            log.info("Temporary directory created at: {}", tempDir.toAbsolutePath());

            ZipEntry entry;
            var foundContentJson = new boolean[]{false, false};
            while ((entry = is.getNextEntry()) != null) {
                var name = entry.getName();
                log.info("Processing ZIP entry: {}", name);
                var fileName = contentProperties.jsonFileName().split("\\.")[0];
                var fileExtension = contentProperties.jsonFileName().split("\\.")[1];
                if (name.equals(fileName + "_de." + fileExtension)) {
                    foundContentJson[0] = true;
                } else if (name.equals(fileName + "_en." + fileExtension)) {
                    foundContentJson[1] = true;
                }

                var filePath = tempDir.resolve(name);
                if (entry.isDirectory()) {
                    log.info("Creating directory: {}", filePath.toAbsolutePath());
                    Files.createDirectories(tempDir.resolve(name));
                } else {
                    log.info("Extracting file to: {}", filePath.toAbsolutePath());
                    Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            if (!foundContentJson[0]) {
                log.error("ZIP file does not contain content_de.json.");
                throw new IllegalArgumentException("ZIP file must contain content_de.json.");
            }

            if (!foundContentJson[1]) {
                log.error("ZIP file does not contain content_en.json.");
                throw new IllegalArgumentException("ZIP file must contain content_en.json.");
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
                var relativeFileLocation = tempDir.relativize(file);
                var resourceLocation = contentPath.resolve(relativeFileLocation);
                log.info("Moving file: {} to {}", file.toAbsolutePath(), resourceLocation);

                var relativeParentDirectory = tempDir.relativize(file.getParent());
                Files.createDirectories(contentPath.resolve(relativeParentDirectory));

                Files.move(file, resourceLocation, StandardCopyOption.REPLACE_EXISTING);
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
