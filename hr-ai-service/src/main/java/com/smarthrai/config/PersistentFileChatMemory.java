package com.smarthrai.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class PersistentFileChatMemory implements ChatMemory {

    private final String sessionsDir;
    private final ObjectMapper objectMapper = new ObjectMapper();
    // Verrous par session pour éviter les accès concurrents sur le même fichier
    private final Map<String, Object> sessionLocks = new ConcurrentHashMap<>();

    public PersistentFileChatMemory(String sessionsDir) {
        this.sessionsDir = sessionsDir;
        File dir = new File(sessionsDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        log.info("Mémoire des sessions initialisée dans le répertoire : {}", sessionsDir);
    }

    private Object getLock(String conversationId) {
        return sessionLocks.computeIfAbsent(conversationId, k -> new Object());
    }

    private File getSessionFile(String conversationId) {
        // Nettoyer l'id de session pour éviter la traversée de répertoires
        String safeId = conversationId.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        return new File(sessionsDir, safeId + ".json");
    }

    private List<MessageDto> readSessionFromFile(File file) {
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(file, new TypeReference<List<MessageDto>>() {});
        } catch (Exception e) {
            log.error("Impossible de lire la session depuis le fichier {} : {}", file.getName(), e.getMessage());
            return new ArrayList<>();
        }
    }

    private void writeSessionToFile(File file, List<MessageDto> messages) {
        try {
            objectMapper.writeValue(file, messages);
        } catch (Exception e) {
            log.error("Impossible d'écrire la session dans le fichier {} : {}", file.getName(), e.getMessage());
        }
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (conversationId == null || messages == null) return;
        synchronized (getLock(conversationId)) {
            File file = getSessionFile(conversationId);
            List<MessageDto> dtos = readSessionFromFile(file);
            for (Message msg : messages) {
                // Éviter de dupliquer si le message est déjà enregistré dans le dernier état
                boolean alreadyExists = dtos.stream().anyMatch(d -> d.getContent().equals(msg.getText()));
                if (!alreadyExists) {
                    dtos.add(new MessageDto(msg.getMessageType().name(), msg.getText()));
                }
            }
            writeSessionToFile(file, dtos);
        }
    }

    @Override
    public List<Message> get(String conversationId, int retrieveSize) {
        if (conversationId == null) return Collections.emptyList();
        synchronized (getLock(conversationId)) {
            File file = getSessionFile(conversationId);
            List<MessageDto> dtos = readSessionFromFile(file);
            if (dtos.isEmpty()) return Collections.emptyList();
            
            List<Message> messages = new ArrayList<>();
            int start = Math.max(0, dtos.size() - retrieveSize);
            for (int i = start; i < dtos.size(); i++) {
                MessageDto dto = dtos.get(i);
                messages.add(createMessage(dto.getType(), dto.getContent()));
            }
            return messages;
        }
    }

    @Override
    public void clear(String conversationId) {
        if (conversationId == null) return;
        synchronized (getLock(conversationId)) {
            File file = getSessionFile(conversationId);
            if (file.exists()) {
                file.delete();
                log.info("Historique de conversation supprimé pour la session : {}", conversationId);
            }
            sessionLocks.remove(conversationId);
        }
    }

    private Message createMessage(String type, String content) {
        if ("USER".equalsIgnoreCase(type)) {
            return new UserMessage(content);
        } else if ("ASSISTANT".equalsIgnoreCase(type)) {
            return new AssistantMessage(content);
        } else if ("SYSTEM".equalsIgnoreCase(type)) {
            return new SystemMessage(content);
        }
        return new UserMessage(content);
    }

    public static class MessageDto {
        private String type;
        private String content;

        public MessageDto() {}

        public MessageDto(String type, String content) {
            this.type = type;
            this.content = content;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
