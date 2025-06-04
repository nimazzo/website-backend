package com.example.websitebackend.content;

import com.example.websitebackend.content.daos.ContentData;
import org.springframework.stereotype.Service;

@Service
public class ContentService {

    private ContentData contentData;

    public ContentData getContent() {
        return contentData;
    }

    public void setContent(ContentData contentData) {
        this.contentData = contentData;
    }

}
