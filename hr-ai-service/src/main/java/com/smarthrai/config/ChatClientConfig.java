package com.smarthrai.config;

import com.smarthrai.mcp.HrMcpTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class ChatClientConfig {

    @Value("classpath:prompts/system-prompt.txt")
    private Resource systemPromptResource;

    @Bean
    public ChatMemory chatMemory() {
        return new PersistentFileChatMemory("data/sessions");
    }

    @Bean
    public ToolCallbackProvider hrMcpToolCallbackProvider(HrMcpTools hrMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(hrMcpTools)
                .build();
    }

    public String getSystemPrompt() {
        try {
            return systemPromptResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "Tu es l'assistant RH de SmartHR. Exécute les outils et réponds précisément.";
        }
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory, ToolCallbackProvider toolCallbackProvider) {
        return builder
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .defaultTools(toolCallbackProvider.getToolCallbacks())
                .build();
    }
}
