package com.example.websitebackend.content;

import com.example.websitebackend.content.daos.ContentData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/content")
    public ResponseEntity<ContentData> getContent() {
        var contentData = contentService.getContent();
        if (contentData != null) {
            return ResponseEntity.ok(contentData);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(path = "/content")
    public ResponseEntity<Void> setContent(@RequestParam("content") MultipartFile contentFile) {
        contentService.setContent(contentFile);
        return ResponseEntity.ok().build();
    }

}
