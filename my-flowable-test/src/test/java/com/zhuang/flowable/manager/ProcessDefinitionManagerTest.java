package com.zhuang.flowable.manager;

import com.zhuang.flowable.MyFlowableTestApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class ProcessDefinitionManagerTest extends MyFlowableTestApplicationTest {

    @Autowired
    ProcessDefinitionManager processDefinitionManager;

    @Test
    void getCurrentTaskDef() {

        processDefinitionManager.getCurrentTaskDef("f3df49d2-35de-11ea-8f64-18602477cc91");

    }
}