package com.wings.aei.controller;

import com.wings.aei.service.ImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class ImageUploadController {

    private final ImageService imageService;

    public ImageUploadController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping(path = "/image", consumes = "multipart/form-data")
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        String url = imageService.processAndStore(file); // 검증+정제+저장
        return ResponseEntity.ok(new UploadResponse(url));
    }

    public record UploadResponse(String url) {}
}