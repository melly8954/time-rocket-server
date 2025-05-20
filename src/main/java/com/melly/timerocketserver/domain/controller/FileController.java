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

import java.io.IOException;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/download/{fileId}")   // 파일 다운로드 API, HTTP 헤더와 바이너리 데이터 직접 반환, 즉 Json 응답이 아니라 공통응답 API 상속불가
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws IOException {
        // 서비스에서 리소스와 원본파일명 같이 받음
        FileDownloadDto fileDownloadDto = fileService.loadFileAsResource(fileId);

        return ResponseEntity.ok()
                // 응답 본문을 '바이너리 데이터' 타입으로 설정 (파일 다운로드 용도)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                // HTTP 헤더에 Content-Disposition 추가 — 브라우저가 첨부파일로 다운로드하도록 원본 파일명 지정
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileDownloadDto.getOriginalName() + "\"")
                // 실제 파일 데이터(Resource)를 응답 본문에 담아 반환
                .body(fileDownloadDto.getResource());
    }
}
