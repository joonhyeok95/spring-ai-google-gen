package com.example.demo.agent.service.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalRequest {

    private String id;
    private String query;
    private String message;
    private LocalDateTime requestedAt;
}
