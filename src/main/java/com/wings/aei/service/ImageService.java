package com.wings.aei.service;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    /** 업로드 파일을 검증/정제/저장하고 퍼블릭 URL을 반환 */
    String processAndStore(MultipartFile file);
}

