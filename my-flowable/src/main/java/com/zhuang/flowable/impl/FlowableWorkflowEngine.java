package com.zhuang.flowable.impl;

import com.zhuang.flowable.AbstractWorkflowEngine;
import com.zhuang.flowable.model.NextTaskInfo;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FlowableWorkflowEngine extends AbstractWorkflowEngine {

    @Autowired
    private RuntimeService runtimeService;

    @Override
    public String startNew(String processDefinitionKey, String userId, String businessKey, Map<String, Object> formData) {
        Map<String, Object> envMap = new HashMap<>();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, envMap);

        return null;
    }

    @Override
    public void save(String taskId, String comment, Map<String, Object> formData) {

    }

    @Override
    public void submit(String taskId, String userId, List<String> nextUsers, String comment, Map<String, Object> formData) {

    }

    @Override
    public void delete(String taskId, String comment, Map<String, Object> formData) {

    }

    @Override
    public NextTaskInfo retrieveNextTaskInfo(String taskId, Map<String, Object> formData) {
        return null;
    }

    @Override
    public Map<String, Object> retrieveFormData(String taskId) {
        return null;
    }
}
