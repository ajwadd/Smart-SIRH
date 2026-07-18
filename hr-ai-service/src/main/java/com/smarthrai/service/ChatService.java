package com.smarthrai.service;

import java.util.UUID;

public interface ChatService {
    String chat(String message, UUID employeeId, String token);
}
