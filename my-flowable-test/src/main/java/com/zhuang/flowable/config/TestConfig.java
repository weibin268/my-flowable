package com.zhuang.flowable.config;

import com.zhuang.flowable.handler.SampleNextTaskUserHandler;
import com.zhuang.flowable.service.SampleUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {

    @Bean
    public SampleUserService sampleUserService() {
        return new SampleUserService();
    }

    @Bean
    public SampleNextTaskUserHandler sampleNextTaskUserHandler() {
        return new SampleNextTaskUserHandler();
    }

}
