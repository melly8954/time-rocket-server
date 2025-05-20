package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.domain.dto.response.FileDownloadDto;
import com.melly.timerocketserver.domain.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/download/{fileId}")   // 파일 다운로드 API, HTTP 헤더와 바이너리 데이터 직접 반환, 즉 Json 응답이 아니라 공통응답 API 상속불가
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws IOException {
        // 서비스에서 리소스와 원본파일명 같이 받음
        FileDownloadDto fileDownloadDto = fileService.loadFileAsResource(fileId);

        // UTF-8 인코딩
        String originalName = fileDownloadDto.getOriginalName();
        String encodedFileName = UriUtils.encode(originalName, StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                // 응답 본문을 '바이너리 데이터' 타입으로 설정 (파일 다운로드 용도)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                // HTTP 헤더에 Content-Disposition 추가 — 브라우저가 첨부파일로 다운로드하도록 원본 파일명 지정
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + safeAsciiFallback(originalName) + "\"; " +
                                "filename*=UTF-8''" + encodedFileName)
                // 실제 파일 데이터(Resource)를 응답 본문에 담아 반환
                .body(fileDownloadDto.getResource());
    }
    // filename → ASCII 전용 Fallback (Postman 이나 일부 클라이언트용)
    // filename* → UTF-8 인코딩 (최신 브라우저 대응)
    private String safeAsciiFallback(String originalName) {
        // 허용할 문자 : 알파벳 대소문자, 숫자, 공백, 하이픈, 언더스코어, 점 등
        // 그 외 문자는 _ 로 대체
        return originalName.replaceAll("[^a-zA-Z0-9 \\-_.\uAC00-\uD7AF]", "_");
    }
}
