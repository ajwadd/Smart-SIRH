package com.smarthrai.security;

public class SecurityContext {
    private static final ThreadLocal<String> tokenHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> originalMessageHolder = new ThreadLocal<>();
    private static final java.util.Map<String, String> lastResolvedEmployees = new java.util.concurrent.ConcurrentHashMap<>();

    public static void setToken(String token) {
        tokenHolder.set(token);
    }

    public static String getToken() {
        return tokenHolder.get();
    }

    public static void setOriginalMessage(String message) {
        originalMessageHolder.set(message);
    }

    public static String getOriginalMessage() {
        return originalMessageHolder.get();
    }

    public static void saveLastResolvedEmployee(String employeeId) {
        String token = getToken();
        if (token != null && employeeId != null) {
            lastResolvedEmployees.put(token, employeeId);
        }
    }

    public static String getLastResolvedEmployee() {
        String token = getToken();
        return token != null ? lastResolvedEmployees.get(token) : null;
    }

    public static void clear() {
        tokenHolder.remove();
        originalMessageHolder.remove();
    }
}
