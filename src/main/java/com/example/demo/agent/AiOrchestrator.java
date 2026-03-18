package com.example.demo.agent;


import org.springframework.stereotype.Service;

import com.example.demo.agent.collaboration.ChartCollaborationService;
import com.example.demo.agent.collaboration.DigitalMarketerCollaborationService;
import com.example.demo.agent.route.IntentClassifier;
import com.example.demo.agent.service.CalendarAgentService;
import com.example.demo.agent.service.GeneralAgentService;
import com.example.demo.agent.service.NotionAgentService;
import com.example.demo.agent.service.RagChatService;
import com.example.demo.agent.service.SqlAgentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiOrchestrator {
    private final IntentClassifier intentClassifier;
    private final SqlAgentService sqlAgentService; // DB 검색 서비스
    private final RagChatService ragAgentService; // RAG 검색 서비스
    private final GeneralAgentService chatAgentService; // LLM 검색 서비스
    private final ChartCollaborationService chartCollaborationService; // 
    private final DigitalMarketerCollaborationService digitalMarketerCollaborationService; // 
    private final CalendarAgentService calendarAgentService;
    private final NotionAgentService notionAgentService;
    
    public Flux<String> handle(String chatId, String userQuery) {
        return intentClassifier.classify(userQuery)
            .flatMapMany(intent -> {
            	log.info("AI Routing 처리: {}", intent);
                if(userQuery.contains("차트")){
                	return chartCollaborationService.createChartCollaborationStream(chatId, userQuery);
                }
                if(userQuery.contains("노션")){
                	return notionAgentService.process(chatId, "dkttkemf@gmail.com");
                }
                if (intent.contains("CALENDAR")) {
                	return calendarAgentService.askStreamCalendar(userQuery, "dkttkemf@gmail.com");
                }
                if (intent.contains("MAIL")) {
                	return calendarAgentService.askStreamEmail(userQuery, "dkttkemf@gmail.com");
                }
                if (intent.contains("DB")) {
                	return sqlAgentService.askStream(chatId, userQuery);
                }
                // 협업이 필요한 경우 (예: "협업" 키워드나 복합 분석 시)
                if (intent.contains("COLLAB") || userQuery.contains("협업")){
                    return digitalMarketerCollaborationService.createCollaborationStream(chatId, userQuery);
                }
                if (intent.contains("RAG")) {
                	return ragAgentService.askStream(chatId, userQuery);
                }
                return chatAgentService.askStream(chatId, userQuery);
            });
    }
}