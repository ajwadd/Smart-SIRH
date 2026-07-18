package com.smarthr.mapper;

import com.smarthr.dto.DocumentDTO;
import com.smarthr.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentDTO toDTO(Document document) {
        if (document == null) {
            return null;
        }

        DocumentDTO dto = new DocumentDTO();
        dto.setId(document.getId());
        dto.setFileName(document.getFileName());
        dto.setFilePath(document.getFilePath());
        dto.setFileSize(document.getFileSize());
        dto.setContentType(document.getContentType());
        dto.setDocumentType(document.getDocumentType());
        dto.setDescription(document.getDescription());

        if (document.getEmployee() != null) {
            dto.setEmployeeId(document.getEmployee().getId());
            dto.setEmployeeFullName(document.getEmployee().getFirstName() + " " + document.getEmployee().getLastName());
        }

        return dto;
    }
}
