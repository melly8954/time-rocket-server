package com.melly.timerocketserver.domain.dto.response;

import lombok.*;
import org.springframework.core.io.Resource;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileDownloadDto {
    private Resource resource;
    private String originalName;
}
