package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.response.FileDownloadDto;
import com.melly.timerocketserver.domain.entity.RocketFileEntity;
import com.melly.timerocketserver.domain.repository.RocketFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {
    private final RocketFileRepository rocketFileRepository;

    public FileService(RocketFileRepository rocketFileRepository) {
        this.rocketFileRepository = rocketFileRepository;
    }

    @Value("${file.access-url-base}")
    private String baseUrl;

    @Value("${file.rocket-file}")
    private String uploadDir1;

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

        // 파일 저장 경로 생성, "업로드 폴더 경로 + 고유 파일 이름"을 합쳐서 전체 파일 경로를 만듬
        Path filePath = uploadPath.resolve(uniqueFileName);

        // 업로드된 파일을 서버에 저장, java.nio.file.Files 클래스의 정적 메서드, 첫 번째 인자(InputStream)에서 읽은 데이터를 두 번째 인자(Path)에 복사해서 저장
        Files.copy(file.getInputStream(), filePath);

        // 저장된 파일의 접근 경로(URL)를 생성하여 반환 (ex. http://localhost:8081/images/uuid.jpg)
        return baseUrl + uniqueFileName;
    }

    // 파일명이 겹치지 않도록 고유한 이름(예: UUID + 확장자)을 만들어 리턴
    private String generateUniqueFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();   // 클라이언트(브라우저)가 업로드한 원본 파일 이름을 반환 (MultipartFile 인터페이스의 메서드)
        String extension = "";  // 파일 확장자를 저장할 변수

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        return UUID.randomUUID() + extension;
    }

    // 첨부파일 다운로드 메서드
    public FileDownloadDto  loadFileAsResource(Long fileId) throws IOException {
        RocketFileEntity fileEntity = rocketFileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("해당 파일 ID에 해당하는 파일이 존재하지 않습니다."));

        Path filePath = Paths.get(uploadDir1).resolve(fileEntity.getUniqueName()).normalize();

        // UrlResource 는 Resource 의 구현체 중 하나로, 파일 시스템 경로나 URL 을 통해 실제 리소스(파일 등)에 접근할 수 있도록 도와줍니다.
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new FileNotFoundException("저장된 파일을 찾을 수 없습니다.");
        }

        return new FileDownloadDto(resource, fileEntity.getOriginalName());
    }
}
