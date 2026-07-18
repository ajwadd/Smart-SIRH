package com.smarthr.dto;

import com.smarthr.enums.DocumentType;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDTO {
    private UUID id;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private DocumentType documentType;
    private String description;
    private UUID employeeId;
    private String employeeFullName;
}
