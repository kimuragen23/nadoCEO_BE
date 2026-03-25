package com.nadoceo.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://localhost:30808/realms/nadoceo}")
    private String issuerUri;

    @Bean
    public OpenAPI nadoceoOpenAPI() {
        String tokenUrl = issuerUri + "/protocol/openid-connect/token";
        String authUrl = issuerUri + "/protocol/openid-connect/auth";

        return new OpenAPI()
                .info(new Info()
                        .title("NADOCEO Coaching AI API")
                        .description("IT 교육 코칭 AI 백엔드 — 소크라테스식 대화, FAQ 벡터 검색, 학습 경로 추적")
                        .version("v1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("keycloak"))
                .components(new Components()
                        .addSecuritySchemes("keycloak", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .flows(new OAuthFlows()
                                        .authorizationCode(new OAuthFlow()
                                                .authorizationUrl(authUrl)
                                                .tokenUrl(tokenUrl))
                                        .password(new OAuthFlow()
                                                .tokenUrl(tokenUrl))))
                );
    }
}
