package com.study.jeonggiju.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// 자기전 휴대폰 X
@EnableJpaAuditing
@Configuration
public class JpaConfig {
}
