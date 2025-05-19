package com.melly.timerocketserver.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.access-url-base}")
    private String baseUrl;

    @Value("${file.rocket-file}")
    private String uploadDir1;

    // 파일명이 겹치지 않도록 고유한 이름(예: UUID + 확장자)을 만들어 리턴
    public String generateUniqueFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        return UUID.randomUUID() + extension;
    }

    // 로컬 디스크 저장
    public String saveRocketFile(MultipartFile file) throws IOException {
        return saveFile(file, uploadDir1);
    }

    private String saveFile(MultipartFile file, String uploadDir) throws IOException {
        // 디렉토리가 없으면 생성
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 고유한 파일 이름 생성
        String uniqueFileName = generateUniqueFileName(file);

        // 파일 저장 경로 생성
        Path filePath = uploadPath.resolve(uniqueFileName);

        // 파일 저장
        Files.copy(file.getInputStream(), filePath);

        // 매핑된 경로 반환**
        return baseUrl + uniqueFileName;
    }
}
