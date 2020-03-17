package com.zhuang.flowable.impl;

import com.zhuang.flowable.MyFlowableTestApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class FlowableWorkflowEngineTest extends MyFlowableTestApplicationTest {

    @Autowired
    FlowableWorkflowEngine flowableWorkflowEngine;

    @Test
    void startNew() {
        Map<String, Object> formData = new HashMap<>();
        String result = flowableWorkflowEngine.startNew("test01", "zwb", "1", formData);
        System.out.println(result);
    }

    @Test
    void startNew2() {
        Map<String, Object> formData = new HashMap<>();
        String result = flowableWorkflowEngine.startNew("CountersignTest", "1", "1", formData);
        System.out.println(result);
    }

    @Test
    void submit() {
        String taskId = "24567abc-67ff-11ea-943f-18602477cc91";
        Map<String, Object> formData = new HashMap<>();
        flowableWorkflowEngine.submit(taskId, "1", Arrays.asList("1"), "aa", formData);
    }

}