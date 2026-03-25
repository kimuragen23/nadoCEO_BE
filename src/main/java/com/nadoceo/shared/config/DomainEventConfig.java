package com.nadoceo.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class DomainEventConfig {
    // Spring ApplicationEventPublisher를 활용한 도메인 이벤트 처리.
    // @Async로 이벤트 리스너의 비동기 처리 활성화.
}
