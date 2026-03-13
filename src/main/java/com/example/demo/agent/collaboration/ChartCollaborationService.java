package com.example.demo.agent.collaboration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import com.example.demo.agent.service.ChartAgentService;
import com.example.demo.agent.service.SqlAgentService;
import com.example.demo.config.AiProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChartCollaborationService {
	private final SqlAgentService sqlAgentService;
    private final ChartAgentService chartAgentService;
    private final AiProperties prompts;

    public Flux<String> createChartCollaborationStream(String chatId, String userQuery) {
        // 1. SQL 에이전트가 DB 데이터를 조회 (승인 절차 포함됨)
        return sqlAgentService.askStream(chatId, userQuery)
            .collectList()
            .map(list -> String.join("", list))
            .flatMapMany(dbResult -> {
                log.info("📊 DB 조회 완료, 시각화 단계 진입. 결과 길이: {}", dbResult.length());
                Resource resource = prompts.getPrompts().getDataScientist();
                String prompt = null;
				try {
					prompt = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                // 2. 조회된 데이터를 시각화 에이전트에게 전달하기 위한 프롬프트 구성
                String chartPrompt = prompt.toString()
                		.replace("{userQuery}", userQuery)
                		.replace("{dbResult}", dbResult);
//                		String.format(
//                    "사용자 질문: [%s]\n" +
//                    "조회된 데이터: [%s]\n\n" +
//                    "위 데이터를 바탕으로 다음 지침을 지켜서 답변해줘:\n" +
//                    "1. 데이터를 분석하여 요약 설명글을 먼저 작성해라.\n" +
//                    "2. 반드시 'generateChart' 도구를 호출하여 시각화 데이터를 생성해라.\n" +
//                    "3. 도구가 반환하는 [CHART_DATA_START]...[CHART_DATA_END] 문자열을 답변 마지막에 절대 생략하지 말고 포함해라.\n" +
//                    "4. 만약 데이터가 너무 많다면 중요한 상위 5~10개 항목만 차트로 구성해라.",
//                    userQuery, dbResult
//                );
                
                // 3. 차트 생성 권한이 있는 에이전트(ChartTool 사용 가능)에게 전달
                return chartAgentService.askStream(chatId, chartPrompt);
            });
    }
}
