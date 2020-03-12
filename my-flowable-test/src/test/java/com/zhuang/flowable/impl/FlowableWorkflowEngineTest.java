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
        String result = flowableWorkflowEngine.startNew("CountersignTest", "1", "1", formData);
        System.out.println(result);
    }

    @Test
    void submit() {
        String taskId = "d6efdd97-6440-11ea-b5a5-60f67771a214";
        Map<String, Object> formData = new HashMap<>();
        formData.put("countersignUsers",Arrays.asList("1"));
        flowableWorkflowEngine.submit(taskId, "1", Arrays.asList("1"), "aa", formData);
    }

}