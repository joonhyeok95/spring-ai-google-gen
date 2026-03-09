package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai")
public class AiProperties {
	private String apiKey;
    private String model = "gemini-1.5-flash"; // 기본값 설정
    private String defaultSystem;

    // Getter, Setter가 반드시 있어야 합니다.
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getDefaultSystem() { return defaultSystem; }
    public void setDefaultSystem(String defaultSystem) { this.defaultSystem = defaultSystem; }
}

