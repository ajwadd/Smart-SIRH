package com.smarthrai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import java.util.Map;
import java.util.List;

public class GroqTest {

    @Test
    public void testGroq() {
        System.out.println("=== DEBUT TEST GROQ ===");
        try {
            String groqBaseUrl = "https://api.groq.com/openai";
            String groqApiKey = System.getenv().getOrDefault("GROQ_API_KEY", "demo");
            String groqModel = "llama-3.3-70b-versatile";

            OpenAiApi groqApi = new OpenAiApi(groqBaseUrl, groqApiKey);
            OpenAiChatOptions groqOptions = new OpenAiChatOptions();
            groqOptions.setModel(groqModel);
            groqOptions.setTemperature(0.0);
            OpenAiChatModel groqChatModel = new OpenAiChatModel(groqApi, groqOptions);

            InMemoryChatMemory chatMemory = new InMemoryChatMemory();
            
            // Simuler l'historique contenant un appel d'outil
            AssistantMessage.ToolCall toolCall = new AssistantMessage.ToolCall("id_1", "function", "getEmployeeProfile", "{\"employeeId\":\"el alami mohamed\"}");
            ToolResponseMessage toolResponse = new ToolResponseMessage(List.of(new ToolResponseMessage.ToolResponse("id_1", "getEmployeeProfile", "Mohamed El Alami a 2 enfants")));
            
            chatMemory.add("hr_global", List.of(
                new UserMessage("combien enfant il a el alami mohamed"),
                new AssistantMessage("", Map.of(), List.of(toolCall)),
                toolResponse
            ));

            ChatClient groqChatClient = ChatClient.builder(groqChatModel)
                    .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                    .build();

            System.out.println("Envoi du message à Groq...");
            ChatResponse chatResponse = groqChatClient.prompt()
                    .user("son salaire")
                    .advisors(a -> a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, "hr_global")
                                   .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                    .call()
                    .chatResponse();

            System.out.println("chatResponse is null? " + (chatResponse == null));
            if (chatResponse != null) {
                System.out.println("getResult() is null? " + (chatResponse.getResult() == null));
                if (chatResponse.getResult() != null) {
                    System.out.println("getOutput() is null? " + (chatResponse.getResult().getOutput() == null));
                    if (chatResponse.getResult().getOutput() != null) {
                        System.out.println("getText() is: " + chatResponse.getResult().getOutput().getText());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur durant le test Groq : " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("=== FIN TEST GROQ ===");
    }
}
