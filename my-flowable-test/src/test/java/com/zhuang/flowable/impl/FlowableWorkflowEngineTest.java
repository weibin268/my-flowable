package com.zhuang.flowable.impl;

import com.zhuang.flowable.MyFlowableTestApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

class FlowableWorkflowEngineTest extends MyFlowableTestApplicationTest {

    @Autowired
    FlowableWorkflowEngine flowableWorkflowEngine;

    @Test
    void startNew() {
        Map<String, Object> formData = new HashMap<>();
        String result = flowableWorkflowEngine.startNew("test01", "1", "1", formData);
        System.out.println(result);
    }
}