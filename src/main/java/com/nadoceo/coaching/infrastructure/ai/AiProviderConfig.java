package com.nadoceo.coaching.infrastructure.ai;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;

/**
 * 멀티 AI 프로바이더 설정.
 * 클래스패스에 존재하는 ChatModel 빈 중 첫 번째를 활성 모델로 선택.
 * 다른 프로바이더 사용 시 build.gradle에서 해당 starter의 주석을 해제하고
 * API 키를 설정한 뒤, 불필요한 starter는 주석 처리.
 */
@Configuration
public class AiProviderConfig {

    @Bean
    @Primary
    public ChatModel activeChatModel(Map<String, ChatModel> chatModels) {
        if (chatModels.isEmpty()) {
            throw new IllegalStateException(
                    "No ChatModel bean found. Check that at least one Spring AI starter " +
                    "is on the classpath and the API key is configured.");
        }
        // 사용 가능한 첫 번째 ChatModel 반환
        return chatModels.values().iterator().next();
    }
}
