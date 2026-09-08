package com.smarthrai;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.smarthrai.mcp.HrMcpTools;

@SpringBootApplication
public class HrAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(HrAiApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.web.client.RestTemplate restTemplate() {
        return new org.springframework.web.client.RestTemplate();
    }

    

}
