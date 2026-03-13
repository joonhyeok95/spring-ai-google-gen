package com.example.demo.agent.tool;


import java.util.List;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ChartTool {

    @Tool(description = "데이터 리스트를 기반으로 Chart.js용 JSON 설정을 생성합니다. 그래프가 필요할 때 사용하세요.")
    public String generateChart(String type, String title, List<String> labels, List<Double> values, ToolContext toolContext) {
        
        String chatId = (String) toolContext.getContext().get("chatId");
        log.info("📊 [차트 생성] ChatId: {}, Type: {}, Title: {}", chatId, type, title);
        
        String labelsJson = "[\"" + String.join("\",\"", labels) + "\"]";
        String valuesJson = values.toString();
        
        // 실제로는 LLM이 이 도구를 호출하며 데이터 구조를 넘겨줍니다.
        // 프론트엔드에서 처리할 수 있도록 특정 태그로 감싸서 반환합니다.
        String result = String.format(
            "\n[CHART_DATA_START]\n" +
    		"{\"type\": \"%s\", \"title\": \"%s\", \"labels\": %s, \"values\": %s}\n" +
    		"[CHART_DATA_END]\n", 
    		type, title, 
    		labelsJson, valuesJson
        );
        log.info("chart data:{}", result);
        return result;
    }
}