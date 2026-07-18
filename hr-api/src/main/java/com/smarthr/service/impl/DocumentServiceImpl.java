package com.smarthr.service.impl;

import com.smarthr.dto.DocumentDTO;
import com.smarthr.entity.Document;
import com.smarthr.entity.Employee;
import com.smarthr.enums.DocumentType;
import com.smarthr.exception.ErrorConstants;
import com.smarthr.exception.ResourceNotFoundException;
import com.smarthr.mapper.DocumentMapper;
import com.smarthr.repository.DocumentRepository;
import com.smarthr.repository.EmployeeRepository;
import com.smarthr.service.DocumentService;
import com.smarthr.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;
    private final MinioService minioService;
    private final DocumentMapper documentMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    @org.springframework.beans.factory.annotation.Value("${hr-ai-service.url:http://localhost:8082/ai}")
    private String aiServiceUrl;

    @Override
    @Transactional
    public DocumentDTO uploadDocument(UUID employeeId, MultipartFile file, DocumentType type, String description) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND, 
                        "Employé introuvable avec l'ID: " + employeeId));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier ne peut pas être vide.");
        }

        String originalFilename = file.getOriginalFilename();
        
        // Générer un chemin d'objet unique pour MinIO : documents/employeeId/UUID_filename
        String objectId = UUID.randomUUID().toString();
        String objectName = String.format("documents/%s/%s_%s", 
                employeeId.toString(), objectId, originalFilename);

        try {
            // Upload sur MinIO
            byte[] fileBytes = file.getBytes();
            minioService.uploadFile(objectName, fileBytes, file.getContentType());

            // Enregistrer dans la base de données
            Document document = Document.builder()
                    .fileName(originalFilename)
                    .filePath(objectName)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .documentType(type)
                    .description(description)
                    .employee(employee)
                    .build();

            Document saved = documentRepository.save(document);
            log.info("Document '{}' créé avec succès en BDD et MinIO pour l'employé {}", originalFilename, employeeId);

            // Indexation vectorielle RAG via appel HTTP vers le microservice IA
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);

                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
                    @Override
                    public String getFilename() {
                        return originalFilename;
                    }
                };
                body.add("file", fileResource);
                body.add("employeeId", employeeId.toString());
                body.add("documentId", saved.getId().toString());
                body.add("type", type.name());

                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
                restTemplate.postForLocation(aiServiceUrl + "/index", requestEntity);
                log.info("Requête d'indexation RAG transmise avec succès au microservice IA.");
            } catch (Exception e) {
                log.error("Échec de l'indexation RAG via le microservice IA pour le document '{}' : {}", originalFilename, e.getMessage());
            }

            return documentMapper.toDTO(saved);

        } catch (IOException e) {
            log.error("Erreur d'obtention des octets du fichier lors du téléversement de '{}'", originalFilename, e);
            throw new RuntimeException("Erreur lors de la lecture du fichier : " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadDocument(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.DOCUMENT_NOT_FOUND, 
                        "Document introuvable avec l'ID: " + documentId));

        return minioService.downloadFile(document.getFilePath());
    }

    @Override
    @Transactional
    public void deleteDocument(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.DOCUMENT_NOT_FOUND, 
                        "Document introuvable avec l'ID: " + documentId));

        // Supprimer le fichier de MinIO
        try {
            minioService.deleteFile(document.getFilePath());
        } catch (Exception e) {
            log.warn("Le fichier physique de MinIO à l'adresse '{}' n'a pas pu être supprimé (peut-être déjà inexistant) : {}", 
                    document.getFilePath(), e.getMessage());
        }

        // Désindexer du Vector Store du microservice IA
        try {
            restTemplate.delete(aiServiceUrl + "/index/" + documentId);
            log.info("Requête de désindexation RAG transmise avec succès au microservice IA.");
        } catch (Exception e) {
            log.warn("Impossible de désindexer le document '{}' du microservice IA : {}", documentId, e.getMessage());
        }

        // Supprimer de la BDD
        documentRepository.delete(document);
        log.info("Document avec l'ID '{}' et son fichier physique supprimés avec succès.", documentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentDTO> getEmployeeDocuments(UUID employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException(ErrorConstants.EMPLOYEE_NOT_FOUND, 
                    "Employé introuvable avec l'ID: " + employeeId);
        }
        return documentRepository.findByEmployeeId(employeeId).stream()
                .map(documentMapper::toDTO)
                .collect(Collectors.toList());
    }
}
