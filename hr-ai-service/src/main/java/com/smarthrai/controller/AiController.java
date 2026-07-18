package com.smarthrai.controller;

import com.smarthrai.service.ChatService;
import com.smarthrai.service.DocumentExtractorService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final ChatService chatService;
    private final DocumentExtractorService documentExtractorService;
    private final VectorStore vectorStore;

    @PostMapping("/index")
    public ResponseEntity<Void> indexDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("employeeId") UUID employeeId,
            @RequestParam("documentId") UUID documentId,
            @RequestParam("type") String type) {

        log.info("Indexation RAG reçue pour le document ID '{}', employé '{}'", documentId, employeeId);
        try {
            byte[] fileBytes = file.getBytes();
            String extractedText = documentExtractorService.extractText(fileBytes, file.getOriginalFilename());
            
            if (extractedText != null && !extractedText.trim().isEmpty()) {
                org.springframework.ai.document.Document vectorDoc = new org.springframework.ai.document.Document(
                        documentId.toString(),
                        extractedText,
                        Map.of(
                                "employeeId", employeeId.toString(),
                                "documentId", documentId.toString(),
                                "documentType", type,
                                "fileName", file.getOriginalFilename()
                        )
                );
                
                // Découpage du document en morceaux (chunks) pour la recherche sémantique
                TokenTextSplitter splitter = new TokenTextSplitter(800, 150, 5, 10000, true);
                List<org.springframework.ai.document.Document> chunks = splitter.apply(List.of(vectorDoc));
                
                log.info("Indexation dans PgVector - Fichier '{}' découpé en {} chunks.", file.getOriginalFilename(), chunks.size());
                vectorStore.add(chunks);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Échec de l'indexation RAG du document '{}' : {}", documentId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/index/{documentId}")
    public ResponseEntity<Void> deleteDocumentIndex(@PathVariable UUID documentId) {
        log.info("Désindexation RAG demandée pour le document ID '{}'", documentId);
        try {
            vectorStore.delete(List.of(documentId.toString()));
            log.info("Document '{}' désindexé avec succès du Vector Store PgVector.", documentId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Échec de la désindexation du document '{}' : {}", documentId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/query")
    public ResponseEntity<QueryResponse> queryAi(@RequestBody QueryRequest request) {
        log.info("Requête RAG reçue pour l'employé '{}'", request.getEmployeeId());
        String aiResponse = chatService.chat(request.getMessage(), request.getEmployeeId(), request.getToken());
        return ResponseEntity.ok(new QueryResponse(aiResponse));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryRequest {
        private String message;
        private UUID employeeId;
        private String token;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryResponse {
        private String response;
    }
}
