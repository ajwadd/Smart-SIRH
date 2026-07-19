package com.smarthrai.service.impl;

import com.smarthrai.config.ChatClientConfig;
import com.smarthrai.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final List<ToolCallbackProvider> toolCallbackProviders;
    private final ChatClientConfig chatClientConfig;

    @Value("${groq.api-key}")
    private String groqApiKey;

    @Value("${groq.base-url}")
    private String groqBaseUrl;

    @Value("${groq.model}")
    private String groqModel;

    private String extractUserIdFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return "anonymous";
        }
        try {
            String[] parts = token.substring(7).split("\\.");
            if (parts.length > 1) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(payload);
                if (node.has("sub")) {
                    return node.get("sub").asText();
                }
                if (node.has("email")) {
                    return node.get("email").asText();
                }
            }
        } catch (Exception e) {
            log.warn("Impossible de décoder le JWT token pour récupérer l'ID utilisateur : {}", e.getMessage());
        }
        return "anonymous";
    }

    private boolean isSimpleGreeting(String msg) {
        if (msg == null) return false;
        String clean = msg.trim().toLowerCase().replaceAll("[^a-zàâäéèêëîïôöûüùç ]", "");
        return clean.equals("bonjour") || clean.equals("hello") || clean.equals("salut") 
                || clean.equals("bonsoir") || clean.equals("coucou") || clean.equals("hi")
                || clean.equals("hey");
    }

    @Override
    public String chat(String message, UUID employeeId, String token) {
        log.info("Traitement d'une requête agent pour l'employé '{}' : '{}'", employeeId, message);
        
        // 1. Intercepter les salutations simples pour économiser les quotas et éviter les outils inutiles
        if (isSimpleGreeting(message)) {
            log.info("Salutation simple détectée ('{}'). Réponse directe via bypass local.", message);
            return "Bonjour ! Je suis l'assistant RH de SmartHR. Comment puis-je vous aider aujourd'hui ?";
        }

        try {
            com.smarthrai.security.SecurityContext.setToken(token);
            com.smarthrai.security.SecurityContext.setOriginalMessage(message);
            if (employeeId != null) {
                com.smarthrai.security.SecurityContext.saveLastResolvedEmployee(employeeId.toString());
            }

            // Isoler l'historique de conversation par utilisateur connecté ET employé cible
            String userId = extractUserIdFromToken(token);
            String sessionId = userId + "_" + (employeeId != null ? employeeId.toString() : "global");
            log.info("Session ID isolée pour la conversation : '{}'", sessionId);

            // TIER 1 : Essayer l'agent principal Gemini (configuré par Spring Boot)
            try {
                log.info("🤖 [Agent SmartHR] Tentative avec l'agent principal Gemini...");
                var response = chatClient.prompt()
                        .system(chatClientConfig.getSystemPrompt()) // Charger dynamiquement le prompt système à chaque appel !
                        .user(message)
                        .advisors(a -> a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId)
                                       .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 4))
                        .call()
                        .chatResponse();

                if (response != null && response.getResult() != null && response.getResult().getOutput() != null) {
                    var assistantMessage = response.getResult().getOutput();
                    if (assistantMessage.getToolCalls() != null && !assistantMessage.getToolCalls().isEmpty()) {
                        assistantMessage.getToolCalls().forEach(toolCall -> {
                            log.info("🤖 [Agent SmartHR - Gemini] Outil exécuté : '{}' avec '{}'", toolCall.name(), toolCall.arguments());
                        });
                    }
                    return response.getResult().getOutput().getText();
                }
            } catch (Exception e1) {
                log.warn("⚠️ Échec de l'agent principal Gemini : {}. Tentative de basculement vers l'agent de repli Groq...", e1.getMessage());

                // TIER 2 : Agent de repli Groq (Llama 3.3) construit de manière dynamique à partir des propriétés
                try {
                    log.info("🤖 [Agent SmartHR] Début Groq fallback...");
                    log.info("🤖 [Agent SmartHR] groqBaseUrl: '{}', groqModel: '{}', apiKey length: {}", groqBaseUrl, groqModel, groqApiKey != null ? groqApiKey.length() : 0);
                    
                    // Collecter et dédoublonner les outils par nom pour éviter "Multiple tools with the same name"
                    java.util.Map<String, org.springframework.ai.model.function.FunctionCallback> uniqueTools = new java.util.HashMap<>();
                    for (ToolCallbackProvider provider : toolCallbackProviders) {
                        if (provider.getToolCallbacks() != null) {
                            for (org.springframework.ai.model.function.FunctionCallback callback : provider.getToolCallbacks()) {
                                uniqueTools.put(callback.getName(), callback);
                            }
                        }
                    }
                    List<org.springframework.ai.model.function.FunctionCallback> toolCallbacks = new java.util.ArrayList<>(uniqueTools.values());

                    OpenAiApi groqApi = new OpenAiApi(groqBaseUrl, groqApiKey);
                    OpenAiChatOptions groqOptions = new OpenAiChatOptions();
                    groqOptions.setModel(groqModel);
                    groqOptions.setTemperature(0.0);
                    groqOptions.setToolCallbacks(toolCallbacks);
                    OpenAiChatModel groqChatModel = new OpenAiChatModel(groqApi, groqOptions);

                    log.info("🤖 [Agent SmartHR] Construction du Groq ChatClient...");
                    ChatClient groqChatClient = ChatClient.builder(groqChatModel)
                            .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                            .build();

                    log.info("🤖 [Agent SmartHR] Envoi du prompt à Groq...");
                    var response = groqChatClient.prompt()
                            .system(chatClientConfig.getSystemPrompt()) // Charger dynamiquement le prompt système à chaque appel !
                            .user(message)
                            .advisors(a -> a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId)
                                           .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 4))
                            .call()
                            .chatResponse();

                    log.info("🤖 [Agent SmartHR] Groq response: {}", response);
                    if (response != null && response.getResult() != null && response.getResult().getOutput() != null) {
                        log.info("🤖 [Agent SmartHR] Groq result output text: '{}'", response.getResult().getOutput().getText());
                        var assistantMessage = response.getResult().getOutput();
                        if (assistantMessage.getToolCalls() != null && !assistantMessage.getToolCalls().isEmpty()) {
                            assistantMessage.getToolCalls().forEach(toolCall -> {
                                log.info("🤖 [Agent SmartHR - Groq Fallback] Outil exécuté : '{}' avec '{}'", toolCall.name(), toolCall.arguments());
                            });
                        }
                        return response.getResult().getOutput().getText();
                    } else {
                        log.warn("🤖 [Agent SmartHR] Groq response or result was null: response={}, result={}", response, response != null ? response.getResult() : null);
                    }
                } catch (Exception e2) {
                    log.error("❌ Échec critique général (Gemini et Groq indisponibles) : {}", e2.getMessage(), e2);
                }
            }

            return "Désolé, les services d'intelligence artificielle (Gemini et Groq) sont momentanément indisponibles. Veuillez réessayer plus tard.";

        } finally {
            com.smarthrai.security.SecurityContext.clear();
        }
    }
}
