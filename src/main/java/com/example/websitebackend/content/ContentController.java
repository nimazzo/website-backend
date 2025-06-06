package com.example.websitebackend.content;

import com.example.websitebackend.content.daos.ContentData;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;

@RestController
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("private/content")
    public ResponseEntity<ContentData> getContent() throws IOException {
        var contentData = contentService.getContent();
        return contentData != null ? ResponseEntity.ok(contentData) : ResponseEntity.notFound().build();
    }

    @GetMapping("/private/content/{fileName:.+}")
    public ResponseEntity<Resource> getPrivateContent(@PathVariable String fileName) throws IOException {

        var resource = contentService.getResource(fileName);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        var mimeType = Files.probeContentType(resource.getFile().toPath());

        return ResponseEntity
                .ok()
                .contentType(mimeType != null ? MediaType.parseMediaType(mimeType) : MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
