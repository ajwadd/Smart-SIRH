package com.smarthr.service;

import com.smarthr.dto.DocumentDTO;
import com.smarthr.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DocumentService {

    /**
     * Téléverse un nouveau document pour un employé et l'enregistre en base de données.
     *
     * @param employeeId L'identifiant de l'employé concerné.
     * @param file Le fichier téléversé.
     * @param type Le type de document.
     * @param description Une description facultative du document.
     * @return Le DTO du document créé.
     */
    DocumentDTO uploadDocument(UUID employeeId, MultipartFile file, DocumentType type, String description);

    /**
     * Récupère le contenu brut d'un document stocké dans MinIO.
     *
     * @param documentId L'identifiant du document.
     * @return Le contenu du fichier en tableau d'octets.
     */
    byte[] downloadDocument(UUID documentId);

    /**
     * Supprime définitivement un document de la base de données et du stockage MinIO.
     *
     * @param documentId L'identifiant du document.
     */
    void deleteDocument(UUID documentId);

    /**
     * Liste l'ensemble des documents d'un employé.
     *
     * @param employeeId L'identifiant de l'employé.
     * @return La liste des documents.
     */
    List<DocumentDTO> getEmployeeDocuments(UUID employeeId);
}
