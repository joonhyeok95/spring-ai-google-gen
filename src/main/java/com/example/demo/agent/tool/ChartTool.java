package com.example.demo.agent.tool;


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ChartTool {

    @Tool(description = "데이터 리스트를 기반으로 Chart.js용 JSON 설정을 생성합니다. 그래프가 필요할 때 사용하세요.")
    public String generateChart(String dataDescription, String chartType, String title) {
        log.info("📊 차트 생성 요청: type={}, title={}", chartType, title);
        
        // 실제로는 LLM이 이 도구를 호출하며 데이터 구조를 넘겨줍니다.
        // 프론트엔드에서 처리할 수 있도록 특정 태그로 감싸서 반환합니다.
        return String.format(
            "\n[CHART_START]\n" +
            "{\n  \"type\": \"%s\",\n  \"title\": \"%s\",\n  \"data\": %s\n}\n" +
            "[CHART_END]\n", 
            chartType, title, dataDescription
        );
    }
}