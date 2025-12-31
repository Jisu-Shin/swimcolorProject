package com.swimcolor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync // 비동기 기능을 활성화합니다!
public class AsyncConfig {
    // 기본 설정으로도 작동하지만, 나중에 쓰레드 풀 설정을 여기서 할 수 있어요.
}