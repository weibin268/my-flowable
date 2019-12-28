package com.zhuang.flowable;


import org.flowable.engine.RuntimeService;
import org.flowable.spring.boot.ProcessEngineServicesAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@AutoConfigureAfter(ProcessEngineServicesAutoConfiguration.class)
public class MyFlowableAutoConfiguration {


}
