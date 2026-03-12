package com.example.demo.agent.service.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ApprovalRequest {

    private String id;
    private String query;
    private String message;
    private LocalDateTime requestedAt;
}
