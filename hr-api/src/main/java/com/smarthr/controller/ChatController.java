package com.smarthr.controller;

import com.smarthr.entity.User;
import com.smarthr.exception.ResourceNotFoundException;
import com.smarthr.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @org.springframework.beans.factory.annotation.Value("${hr-ai-service.url:http://localhost:8082/ai}")
    private String aiServiceUrl;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", 
                                "Utilisateur introuvable avec l'identifiant : " + username)));

        // Les comptes ADMIN ou RH n'ont pas obligatoirement de profil Employee physique rattaché
        UUID employeeId = (user.getEmployee() != null) ? user.getEmployee().getId() : null;
        
        // Appel REST vers le microservice hr-ai-service
        QueryRequest queryRequest = new QueryRequest(request.getMessage(), employeeId, authHeader);
        QueryResponse queryResponse = restTemplate.postForObject(aiServiceUrl + "/query", queryRequest, QueryResponse.class);
        
        String aiResponse = queryResponse != null ? queryResponse.getResponse() : "Erreur : Impossible de contacter le microservice d'IA.";

        return ResponseEntity.ok(new ChatResponse(aiResponse));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRequest {
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatResponse {
        private String response;
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
