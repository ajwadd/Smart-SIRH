package com.smarthr.entity;

import com.smarthr.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "documents", indexes = {
    @Index(name = "idx_document_employee", columnList = "employee_id"),
    @Index(name = "idx_document_type", columnList = "document_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document extends BaseEntity {

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 20)
    private DocumentType documentType;

    @Column(name = "description")
    private String description;

    // ========================
    // RELATIONS
    // ========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
