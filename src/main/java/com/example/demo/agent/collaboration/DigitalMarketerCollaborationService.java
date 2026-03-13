package com.example.demo.agent.collaboration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import com.example.demo.agent.service.GeneralAgentService;
import com.example.demo.agent.service.SqlAgentService;
import com.example.demo.config.AiProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class DigitalMarketerCollaborationService {

	private final SqlAgentService sqlAgentService;
    private final GeneralAgentService generalAgentService;
    private final AiProperties prompts;
    
	public Flux<String> createCollaborationStream(String chatId, String userQuery) {
        // 1. 먼저 SQL 에이전트에게 데이터를 가져오게 합니다. (스트림이 아닌 단일 텍스트로 취합)
        return sqlAgentService.askStream(chatId, userQuery)
            .collectList()
            .map(list -> String.join("", list))
            .flatMapMany(dbResult -> {
            	Resource resource = prompts.getPrompts().getDigitalMarketer();
                // 2. DB 결과를 프롬프트에 섞어서 General 에이전트에게 넘깁니다.
                String prompt = null;
				try {
					prompt = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

                String collaborationPrompt = prompt.toString()
                		.replace("{userQuery}", userQuery)
                		.replace("{dbResult}", dbResult);
//                String.format(
//                		prompt.toString(),
//                    dbResult, userQuery
//                );
                
                return generalAgentService.askStream(chatId, collaborationPrompt);
            });
    }

}
