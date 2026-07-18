package com.smarthrai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthrai.service.HrEmployeeService;
import com.smarthrai.service.WebSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HrEmployeeServiceImpl implements HrEmployeeService {

    private final VectorStore vectorStore;
    private final WebSearchService webSearchService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${api.service.url:http://smarthr-api:8081/api}")
    private String API_SERVICE_URL;

    @Value("${ml.service.url:http://smarthr-ml-service:8083/predict}")
    private String mlUrl;

    private boolean isUserAuthorized(String targetEmployeeId) {
        String token = com.smarthrai.security.SecurityContext.getToken();
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("Aucun token Bearer trouvé dans le contexte de sécurité.");
            return false;
        }
        try {
            String[] parts = token.substring(7).split("\\.");
            if (parts.length > 1) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                JsonNode node = objectMapper.readTree(payload);
                
                // Extraire le sub (ID de l'employé connecté)
                String loggedInEmployeeId = node.has("sub") ? node.get("sub").asText() : null;
                
                // Extraire les rôles
                List<String> roles = new ArrayList<>();
                if (node.has("roles")) {
                    node.get("roles").forEach(r -> roles.add(r.asText().toUpperCase()));
                } else if (node.has("authorities")) {
                    node.get("authorities").forEach(a -> roles.add(a.asText().toUpperCase()));
                }
                
                boolean isAdminOrHr = roles.stream().anyMatch(r -> 
                        r.contains("ADMIN") || r.contains("HR")
                );
                
                log.info("Vérification JWT - Utilisateur: '{}', Rôles: {}, Cible résolue: '{}', Est Admin/HR: {}", 
                        loggedInEmployeeId, roles, targetEmployeeId, isAdminOrHr);

                // L'utilisateur est autorisé s'il est Admin/HR, OU s'il consulte son propre dossier
                if (isAdminOrHr) {
                    return true;
                }
                if (targetEmployeeId != null && loggedInEmployeeId != null && targetEmployeeId.equalsIgnoreCase(loggedInEmployeeId)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Erreur lors de la validation des rôles et permissions : {}", e.getMessage());
        }
        return false;
    }

    private JsonNode createErrorResponse(String errorMsg, String details) {
        return objectMapper.createObjectNode()
                .put("error", errorMsg)
                .put("details", details);
    }

    @Override
    public String searchEmployeeDocuments(String query, String employeeId) {
        final String targetEmployeeId = resolveEmployeeId(employeeId, query);
        log.info("Service searchEmployeeDocuments pour l'employé '{}' et la requête '{}'", targetEmployeeId, query);
        
        // Sécurité RAG : Verrouiller les documents privés s'il s'agit d'un autre employé sans droits admin/RH
        if (targetEmployeeId != null && !targetEmployeeId.trim().isEmpty() && !isPlaceholder(targetEmployeeId)) {
            if (!isUserAuthorized(targetEmployeeId)) {
                return "Accès refusé. Vous n'avez pas l'autorisation de consulter les documents de cet employé.";
            }
        }
        
        try {
            SearchRequest searchRequest = SearchRequest.builder().query(query).topK(2).build();
            List<org.springframework.ai.document.Document> similarDocs = vectorStore.similaritySearch(searchRequest);

            boolean noEmployeeContext = targetEmployeeId == null || targetEmployeeId.trim().isEmpty() || isPlaceholder(targetEmployeeId);
            String results = similarDocs.stream()
                    .filter(doc -> {
                        if (noEmployeeContext) {
                            return true;
                        }
                        Object docEmpId = doc.getMetadata().get("employeeId");
                        return docEmpId == null || docEmpId.toString().equalsIgnoreCase(targetEmployeeId);
                    })
                    .map(org.springframework.ai.document.Document::getText)
                    .collect(Collectors.joining("\n---\n"));

            return results.isEmpty() ? "Aucun document interne trouvé." : results;
        } catch (Exception e) {
            log.error("Erreur de recherche sémantique : {}", e.getMessage());
            return "Erreur lors de la recherche documentaire : " + e.getMessage();
        }
    }

    @Override
    public Map<String, Object> predictEmployeeChurn(String employeeId) {
        employeeId = resolveEmployeeId(employeeId, null);
        log.info("Service predictEmployeeChurn appelé pour l'employé '{}'", employeeId);
        
        // Seuls les Admins/RH peuvent prédire l'attrition des employés
        if (!isUserAuthorized(null)) {
            return Map.of("error", "Accès refusé", "details", "Seuls les membres RH ou Administrateurs sont autorisés à consulter les risques d'attrition.");
        }

        try {
            // 1. Récupérer les informations de l'employé via smarthr-api
            String employeeUrl = API_SERVICE_URL + "/employees/" + employeeId;
            JsonNode emp = fetchJsonNode(employeeUrl);
            if (emp.has("error")) {
                return Map.of("error", "Impossible de récupérer les informations de l'employé", "details", emp.get("details").asText());
            }

            String firstName = emp.has("firstName") ? emp.get("firstName").asText() : "";
            String lastName = emp.has("lastName") ? emp.get("lastName").asText() : "";
            String fullName = firstName + " " + lastName;
            String dobStr = emp.has("dateOfBirth") ? emp.get("dateOfBirth").asText() : null;
            String hireStr = emp.has("hireDate") ? emp.get("hireDate").asText() : null;

            // Calculer l'âge
            int age = 30; // valeur par défaut
            if (dobStr != null && dobStr.contains("-")) {
                int birthYear = Integer.parseInt(dobStr.split("-")[0]);
                age = java.time.LocalDate.now().getYear() - birthYear;
            }

            // Calculer l'ancienneté
            int yearsAtCompany = 3; // valeur par défaut
            if (hireStr != null && hireStr.contains("-")) {
                int hireYear = Integer.parseInt(hireStr.split("-")[0]);
                yearsAtCompany = java.time.LocalDate.now().getYear() - hireYear;
            }

            // 2. Récupérer le salaire et les promotions de l'employé
            double monthlyIncome = 10000.0; // valeur par défaut
            int numPromotions = 0;
            try {
                String contractsUrl = API_SERVICE_URL + "/contracts/employee/" + employeeId;
                String contractsJson = getWithAuth(contractsUrl, String.class);
                if (contractsJson != null) {
                    JsonNode contracts = objectMapper.readTree(contractsJson);
                    if (contracts.isArray()) {
                        numPromotions = Math.max(0, contracts.size() - 1);
                        for (JsonNode contract : contracts) {
                            if (contract.has("salary")) {
                                double salary = contract.get("salary").asDouble();
                                if (salary > 0) {
                                    monthlyIncome = salary;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Échec de la récupération des contrats pour la prédiction : {}", e.getMessage());
            }

            // 3. Récupérer l'équilibre vie pro/perso basé sur la consommation des congés
            int workLifeBalance = 3; // valeur par défaut (bon équilibre)
            try {
                String balanceUrl = API_SERVICE_URL + "/leaves/employee/" + employeeId + "/balance";
                String balanceJson = getWithAuth(balanceUrl, String.class);
                if (balanceJson != null) {
                    JsonNode balance = objectMapper.readTree(balanceJson);
                    double totalAcquired = balance.has("totalAcquired") ? balance.get("totalAcquired").asDouble() : 0.0;
                    double totalConsumed = balance.has("totalConsumed") ? balance.get("totalConsumed").asDouble() : 0.0;
                    if (totalAcquired > 0) {
                        double ratio = totalConsumed / totalAcquired;
                        if (ratio < 0.15) {
                            workLifeBalance = 1; // mauvais équilibre (surmenage)
                        } else if (ratio < 0.35) {
                            workLifeBalance = 2; // équilibre moyen
                        } else if (ratio < 0.75) {
                            workLifeBalance = 3; // bon équilibre
                        } else {
                            workLifeBalance = 4; // excellent équilibre
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Échec de la récupération du solde de congés pour la prédiction : {}", e.getMessage());
            }

            // 4. Récupérer les heures supplémentaires basées sur l'assiduité (check-outs tardifs)
            int overtime = 0;
            try {
                String attendanceUrl = API_SERVICE_URL + "/attendance/employee/" + employeeId;
                String attendanceJson = getWithAuth(attendanceUrl, String.class);
                if (attendanceJson != null) {
                    JsonNode attendances = objectMapper.readTree(attendanceJson);
                    if (attendances.isArray()) {
                        int lateCheckOuts = 0;
                        for (JsonNode att : attendances) {
                            if (att.has("checkOut") && !att.get("checkOut").isNull()) {
                                String checkOutStr = att.get("checkOut").asText();
                                if (checkOutStr.contains(":")) {
                                    int hour = Integer.parseInt(checkOutStr.split(":")[0]);
                                    if (hour >= 18) {
                                        lateCheckOuts++;
                                    }
                                }
                            }
                        }
                        if (lateCheckOuts >= 3) {
                            overtime = 1;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Échec de la récupération de la présence pour la prédiction : {}", e.getMessage());
            }

            // 5. Niveau de satisfaction estimé sur le niveau de salaire
            int jobSatisfaction = 3;
            if (monthlyIncome < 8000.0) {
                jobSatisfaction = 2;
            } else if (monthlyIncome > 20000.0) {
                jobSatisfaction = 4;
            }

            // Interroger le microservice de ML Python
            Map<String, Object> requestPayload = Map.of(
                    "age", age,
                    "monthly_income", monthlyIncome,
                    "years_at_company", yearsAtCompany,
                    "job_satisfaction", jobSatisfaction,
                    "work_life_balance", workLifeBalance,
                    "overtime", overtime,
                    "num_promotions", numPromotions);

            log.info("Envoi de la requête prédictive ML : {}", requestPayload);
            Map<String, Object> predictionResult = restTemplate.postForObject(mlUrl, requestPayload, Map.class);
            if (predictionResult == null) {
                return Map.of("error", "Le service de prédiction ML n'a retourné aucun résultat.");
            }

            double probability = (Double) predictionResult.get("probability");
            String riskLevel = (String) predictionResult.get("risk_level");
            List<String> factors = (List<String>) predictionResult.get("factors");

            return Map.of(
                    "employeeId", employeeId,
                    "employeeFullName", fullName,
                    "riskLevel", riskLevel,
                    "probability", probability,
                    "influencingFactors", factors
            );

        } catch (Exception e) {
            log.error("Échec de la prédiction de churn pour l'employé : {}", e.getMessage());
            return Map.of("error", "Impossible d'exécuter la prédiction ML", "details", e.getMessage());
        }
    }

    @Override
    public JsonNode searchEmployees(String keyword) {
        log.info("Service searchEmployees appelé avec le mot-clé '{}'", keyword);
        try {
            String url = API_SERVICE_URL + "/employees?keyword=" + keyword;
            JsonNode result = fetchJsonNode(url);

            if (isEmptyResult(result)) {
                if (keyword != null && keyword.trim().contains(" ")) {
                    String[] parts = keyword.trim().split("\\s+");
                    for (String part : parts) {
                        if (part.length() <= 2)
                            continue;
                        log.info("Aucun résultat pour '{}'. Tentative avec le mot '{}'", keyword, part);
                        String fallbackUrl = API_SERVICE_URL + "/employees?keyword=" + part;
                        JsonNode fallbackResult = fetchJsonNode(fallbackUrl);
                        if (!isEmptyResult(fallbackResult)) {
                            return fallbackResult;
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Échec de la recherche de collaborateurs : {}", e.getMessage());
            return objectMapper.createObjectNode()
                    .put("error", "Impossible de rechercher les collaborateurs")
                    .put("details", e.getMessage());
        }
    }

    @Override
    public JsonNode getEmployeeLeaveBalance(String employeeId) {
        employeeId = resolveEmployeeId(employeeId, null);
        log.info("Service getEmployeeLeaveBalance pour l'employé '{}'", employeeId);
        
        if (!isUserAuthorized(employeeId)) {
            return createErrorResponse("Accès refusé", "Vous n'avez pas l'autorisation de consulter le solde de congés de cet employé.");
        }
        
        String url = API_SERVICE_URL + "/leaves/employee/" + employeeId + "/balance";
        return fetchJsonNode(url);
    }

    @Override
    public JsonNode getEmployeeLeaves(String employeeId) {
        employeeId = resolveEmployeeId(employeeId, null);
        log.info("Service getEmployeeLeaves pour l'employé '{}'", employeeId);
        
        if (!isUserAuthorized(employeeId)) {
            return createErrorResponse("Accès refusé", "Vous n'avez pas l'autorisation de consulter l'historique des congés de cet employé.");
        }
        
        String url = API_SERVICE_URL + "/leaves/employee/" + employeeId;
        return fetchJsonNode(url);
    }

    @Override
    public JsonNode getEmployeeProfile(String employeeId) {
        employeeId = resolveEmployeeId(employeeId, null);
        log.info("Service getEmployeeProfile pour l'employé '{}'", employeeId);
        
        if (!isUserAuthorized(employeeId)) {
            return createErrorResponse("Accès refusé", "Vous n'avez pas l'autorisation de consulter le profil de cet employé.");
        }
        
        String url = API_SERVICE_URL + "/employees/" + employeeId;
        JsonNode profile = fetchJsonNode(url);
        
        // Enrichir le profil avec le salaire et les contrats
        if (profile.isObject() && !profile.has("error")) {
            try {
                String contractsUrl = API_SERVICE_URL + "/contracts/employee/" + employeeId;
                String contractsJson = getWithAuth(contractsUrl, String.class);
                if (contractsJson != null) {
                    JsonNode contracts = objectMapper.readTree(contractsJson);
                    ((com.fasterxml.jackson.databind.node.ObjectNode) profile).set("contracts", contracts);
                }
            } catch (Exception e) {
                log.warn("Impossible d'enrichir le profil avec les contrats : {}", e.getMessage());
            }
        }
        
        return profile;
    }

    @Override
    public JsonNode getDashboardStats() {
        log.info("Service getDashboardStats appelé");
        
        if (!isUserAuthorized(null)) {
            return createErrorResponse("Accès refusé", "Seuls les membres RH ou Administrateurs peuvent consulter le tableau de bord.");
        }
        
        String url = API_SERVICE_URL + "/dashboard/summary";
        return fetchJsonNode(url);
    }

    private JsonNode fetchJsonNode(String url) {
        try {
            String response = getWithAuth(url, String.class);
            if (response != null && !response.trim().isEmpty()) {
                return objectMapper.readTree(response);
            }
        } catch (Exception e) {
            log.error("Échec de récupération JSON depuis {} : {}", url, e.getMessage());
            return objectMapper.createObjectNode()
                    .put("error", "Service indisponible ou erreur de communication")
                    .put("details", e.getMessage());
        }
        return objectMapper.createObjectNode();
    }

    private boolean isEmptyResult(JsonNode result) {
        if (result == null || result.isEmpty()) {
            return true;
        }
        if (result.has("content") && result.get("content").isArray() && result.get("content").isEmpty()) {
            return true;
        }
        if (result.has("totalElements") && result.get("totalElements").asInt() == 0) {
            return true;
        }
        return false;
    }

    private <T> T getWithAuth(String url, Class<T> responseType) {
        String token = com.smarthrai.security.SecurityContext.getToken();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        if (token != null && !token.trim().isEmpty()) {
            headers.set("Authorization", token);
        }
        org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
        return restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, responseType).getBody();
    }

    private boolean isPlaceholder(String value) {
        if (value == null || value.trim().isEmpty())
            return true;
        String v = value.trim().toUpperCase();
        return v.matches("X{2,}") || v.equals("EMPLOYEE_ID") || v.equals("UNKNOWN") || v.equals("NULL");
    }

    private String resolveEmployeeId(String employeeIdOrNumber) {
        if (isPlaceholder(employeeIdOrNumber)) {
            return employeeIdOrNumber;
        }

        try {
            java.util.UUID.fromString(employeeIdOrNumber);
            com.smarthrai.security.SecurityContext.saveLastResolvedEmployee(employeeIdOrNumber);
            return employeeIdOrNumber;
        } catch (IllegalArgumentException e) {
            // Pas un UUID valide
        }

        log.info("Tentative de résolution de l'identifiant pour '{}'", employeeIdOrNumber);
        try {
            String url = API_SERVICE_URL + "/employees?keyword=" + employeeIdOrNumber;
            JsonNode root = fetchJsonNode(url);
            JsonNode content = root.get("content");
            if (content != null && content.isArray() && content.size() > 0) {
                String resolvedId = content.get(0).get("id").asText();
                log.info("Identifiant résolu via JSON : {} -> {}", employeeIdOrNumber, resolvedId);
                com.smarthrai.security.SecurityContext.saveLastResolvedEmployee(resolvedId);
                return resolvedId;
            }
        } catch (Exception e) {
            log.error("Échec de la résolution de l'identifiant pour '{}' : {}", employeeIdOrNumber, e.getMessage());
        }

        return employeeIdOrNumber;
    }

    private String resolveEmployeeId(String employeeIdOrNumber, String query) {
        String resolved = resolveEmployeeId(employeeIdOrNumber);

        boolean stillUnresolved = isPlaceholder(resolved);
        if (!stillUnresolved) {
            try {
                java.util.UUID.fromString(resolved);
                stillUnresolved = false;
            } catch (IllegalArgumentException e) {
                stillUnresolved = true;
            }
        }

        if (stillUnresolved) {
            String originalMessage = com.smarthrai.security.SecurityContext.getOriginalMessage();
            String searchText = (originalMessage != null && !originalMessage.trim().isEmpty()) ? originalMessage
                    : query;

            if (searchText != null && !searchText.trim().isEmpty()) {
                log.info("L'identifiant n'a pas pu être résolu (valeur: '{}'). Tentative d'extraction depuis le message original : '{}'",
                        resolved, searchText);
                java.util.Set<String> stopWords = java.util.Set.of(
                        "salaire", "contrat", "employé", "collaborateur", "quel", "quelle",
                        "est", "de", "du", "la", "le", "les", "des", "un", "une",
                        "son", "sa", "ses", "mon", "ma", "mes", "monsieur", "madame",
                        "mademoiselle", "pour", "dans", "avec", "sur", "par", "que",
                        "qui", "quoi", "comment", "combien", "congé", "congés",
                        "document", "documents", "information", "informations",
                        "brut", "net", "mensuel", "annuel");

                String[] words = searchText.split("[\\s',.?!]+");
                for (String word : words) {
                    if (word.length() > 2 && !stopWords.contains(word.toLowerCase())) {
                        try {
                            String url = API_SERVICE_URL + "/employees?keyword=" + word;
                            JsonNode root = fetchJsonNode(url);
                            JsonNode content = root.get("content");
                            if (content != null && content.isArray() && content.size() > 0) {
                                String resolvedId = content.get(0).get("id").asText();
                                log.info("Identifiant résolu par mot clé du message original '{}' : {}", word, resolvedId);
                                com.smarthrai.security.SecurityContext.saveLastResolvedEmployee(resolvedId);
                                return resolvedId;
                            }
                        } catch (Exception e) {
                            // ignorer et essayer le mot suivant
                        }
                    }
                }
            }

            String lastResolved = com.smarthrai.security.SecurityContext.getLastResolvedEmployee();
            if (lastResolved != null) {
                log.info("Identifiant résolu à partir du dernier résolu en session : {}", lastResolved);
                return lastResolved;
            }
        }
        return resolved;
    }
}
