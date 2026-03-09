package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
	private String apiKey;
    private String model = "gemini-1.5-flash"; // 기본값 설정
    private String defaultSystem;
}

