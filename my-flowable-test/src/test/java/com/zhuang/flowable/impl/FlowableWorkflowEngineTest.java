package com.zhuang.flowable.impl;

import com.zhuang.flowable.MyFlowableTestApplicationTest;
import com.zhuang.flowable.constant.ProcessChoiceOptions;
import com.zhuang.flowable.model.NextTaskInfo;
import com.zhuang.flowable.util.ParamsUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class FlowableWorkflowEngineTest extends MyFlowableTestApplicationTest {

    @Autowired
    FlowableWorkflowEngine flowableWorkflowEngine;

    @Test
    void start() {
        Map<String, Object> params = new HashMap<>();
        String result = flowableWorkflowEngine.start("test01", "zwb", "1", params);
        System.out.println(result);
    }

    @Test
    void start2() {
        Map<String, Object> params = new HashMap<>();
        String result = flowableWorkflowEngine.start("CountersignTest", "1", "1", params);
        System.out.println(result);
    }

    @Test
    void submit() {
        String taskId = "c1b3b89d-6804-11ea-9b7a-18602477cc91";
        Map<String, Object> params = new HashMap<>();
        params.put("amount", 10000);
        flowableWorkflowEngine.submit(taskId, "zs", Arrays.asList("1"), "bb", params);
    }

    @Test
    void retrieveNextTaskInfo() {
        Map<String, Object> params = new HashMap<>();
        ParamsUtils.setChoice(params, ProcessChoiceOptions.AGREE);
        params.put("amount", 100);
        NextTaskInfo nextTaskInfo = flowableWorkflowEngine.retrieveNextTaskInfo("27fd216b-6115-11eb-99a8-b42e995cab02", params);
        System.out.println(nextTaskInfo);
    }

    @Test
    void retrieveParams() {
        Map<String, Object> params = flowableWorkflowEngine.retrieveParams("8e5d4359-685f-11ea-87ef-34f39a2852bc");
        System.out.println(params);
    }
}
