package com.smarthrai.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthrai.service.HrEmployeeService;
import com.smarthrai.service.WebSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class HrMcpTools {

    private final HrEmployeeService hrEmployeeService;
    private final WebSearchService webSearchService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode createErrorResponse(String errorMsg, String details) {
        return objectMapper.createObjectNode()
                .put("error", errorMsg)
                .put("details", details);
    }

    @Tool(description = "Rechercher des informations dans les documents internes de l'entreprise (règlement intérieur, politiques de remboursement) ou d'un employé (contrats, paie).")
    public String searchEmployeeDocuments(
            @ToolParam(description = "La requête textuelle à rechercher dans les documents.") String query,
            @ToolParam(description = "Optionnel. Le nom, prénom, matricule ou UUID de l'employé. Laissez vide pour les recherches générales de l'entreprise.") String employeeId) {
        try {
            log.info("MCP Tool searchEmployeeDocuments appelé pour '{}' avec la requête '{}'", employeeId, query);
            return hrEmployeeService.searchEmployeeDocuments(query, employeeId);
        } catch (Exception e) {
            log.error("Erreur dans searchEmployeeDocuments : {}", e.getMessage());
            return "Une erreur est survenue lors de la recherche documentaire : " + e.getMessage();
        }
    }

    @Tool(description = "Effectuer une recherche web externe sur Google/Bing via SearXNG. À utiliser quand les outils internes (searchEmployeeDocuments, getEmployeeProfile, etc.) ne contiennent pas l'information recherchée, ou pour des questions sur le droit du travail, les lois, l'actualité, ou toute information générale externe à l'entreprise.")
    public String searchWebInfo(
            @ToolParam(description = "La requête de recherche web externe.") String query) {
        try {
            log.info("MCP Tool searchWebInfo appelé pour la requête '{}'", query);
            return webSearchService.searchWeb(query);
        } catch (Exception e) {
            log.error("Erreur dans searchWebInfo : {}", e.getMessage());
            return "Une erreur est survenue lors de la recherche web externe : " + e.getMessage();
        }
    }

    @Tool(description = "Récupérer les statistiques globales et KPIs du tableau de bord RH (masse salariale, salaire net moyen, taux de présence)")
    public JsonNode getDashboardStats() {
        try {
            log.info("MCP Tool getDashboardStats appelé");
            return hrEmployeeService.getDashboardStats();
        } catch (Exception e) {
            log.error("Erreur dans getDashboardStats : {}", e.getMessage());
            return createErrorResponse("Erreur de récupération", e.getMessage());
        }
    }

    @Tool(description = "Calculer la probabilité d'attrition/démission d'un employé en interrogeant le modèle de Machine Learning.")
    public Map<String, Object> predictEmployeeChurn(
            @ToolParam(description = "Le nom, prénom, matricule ou UUID de l'employé.") String employeeId) {
        try {
            log.info("MCP Tool predictEmployeeChurn appelé pour l'employé '{}'", employeeId);
            return hrEmployeeService.predictEmployeeChurn(employeeId);
        } catch (Exception e) {
            log.error("Erreur dans predictEmployeeChurn : {}", e.getMessage());
            return Map.of("error", "Une erreur est survenue lors de la prédiction", "details", e.getMessage());
        }
    }

    @Tool(description = "Rechercher des collaborateurs par nom ou prénom pour obtenir leurs identifiants (employeeId), e-mail, département et poste")
    public JsonNode searchEmployees(
            @ToolParam(description = "Le nom ou prénom recherché.") String keyword) {
        try {
            log.info("MCP Tool searchEmployees appelé avec le mot-clé '{}'", keyword);
            return hrEmployeeService.searchEmployees(keyword);
        } catch (Exception e) {
            log.error("Erreur dans searchEmployees : {}", e.getMessage());
            return createErrorResponse("Erreur de recherche d'employés", e.getMessage());
        }
    }

    @Tool(description = "Consulter le solde de congés (jours restants, acquis, pris) d'un employé.")
    public JsonNode getEmployeeLeaveBalance(
            @ToolParam(description = "Le nom, prénom, matricule ou UUID de l'employé.") String employeeId) {
        try {
            log.info("MCP Tool getEmployeeLeaveBalance appelé pour l'employé '{}'", employeeId);
            return hrEmployeeService.getEmployeeLeaveBalance(employeeId);
        } catch (Exception e) {
            log.error("Erreur dans getEmployeeLeaveBalance : {}", e.getMessage());
            return createErrorResponse("Erreur de solde de congés", e.getMessage());
        }
    }

    @Tool(description = "Consulter l'historique complet des demandes de congés (dates, statut, motif) d'un employé.")
    public JsonNode getEmployeeLeaves(
            @ToolParam(description = "Le nom, prénom, matricule ou UUID de l'employé.") String employeeId) {
        try {
            log.info("MCP Tool getEmployeeLeaves appelé pour l'employé '{}'", employeeId);
            return hrEmployeeService.getEmployeeLeaves(employeeId);
        } catch (Exception e) {
            log.error("Erreur dans getEmployeeLeaves : {}", e.getMessage());
            return createErrorResponse("Erreur d'historique de congés", e.getMessage());
        }
    }

    @Tool(description = "Consulter le profil, le contrat, le salaire, et les informations personnelles détaillées d'un employé (statut familial, adresse, nombre d'enfants, téléphone, CIN, RIB, CNSS, date d'embauche, salaire de base brut, contrat).")
    public JsonNode getEmployeeProfile(
            @ToolParam(description = "Le nom, prénom, matricule ou UUID de l'employé.") String employeeId) {
        try {
            log.info("MCP Tool getEmployeeProfile appelé pour l'employé '{}'", employeeId);
            return hrEmployeeService.getEmployeeProfile(employeeId);
        } catch (Exception e) {
            log.error("Erreur dans getEmployeeProfile : {}", e.getMessage());
            return createErrorResponse("Erreur de profil", e.getMessage());
        }
    }
}
