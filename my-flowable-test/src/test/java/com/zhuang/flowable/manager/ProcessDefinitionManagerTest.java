package com.zhuang.flowable.manager;

import com.zhuang.flowable.MyFlowableTestApplicationTest;
import com.zhuang.flowable.model.TaskDef;
import org.flowable.bpmn.model.FlowNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

class ProcessDefinitionManagerTest extends MyFlowableTestApplicationTest {

    @Autowired
    ProcessDefinitionManager processDefinitionManager;

    @Test
    void getTaskDefByTaskId() {
        //TaskDef taskDef = processDefinitionManager.getTaskDefByTaskId("d6fa7d9e-6446-11ea-b38d-60f67771a214");
        TaskDef taskDef = processDefinitionManager.getTaskDefByTaskId("f66dc25d-67f8-11ea-a302-18602477cc91");
        System.out.println(taskDef);
    }

    @Test
    void getNextTaskDefByTaskId() {
        Map<String, Object> params = new HashMap<>();
       // processDefinitionManager.getNextTaskDefByTaskId("d6efdd97-6440-11ea-b5a5-60f67771a214", params);
    }

    @Test
    public void getFlowNodeByTaskId() {
        FlowNode flowNode = processDefinitionManager.getFlowNodeByTaskId("f3df49d2-35de-11ea-8f64-18602477cc91");
        System.out.println(flowNode);
    }
}