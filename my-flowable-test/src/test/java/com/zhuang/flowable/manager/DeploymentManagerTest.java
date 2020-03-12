package com.zhuang.flowable.manager;

import com.zhuang.flowable.MyFlowableTestApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentManagerTest extends MyFlowableTestApplicationTest {

    @Autowired
    DeploymentManager deploymentManager;

    @Test
    void deployByClasspathResource() {
        deploymentManager.deployByClasspathResource("countersign-test.bpmn","countersign-test");
    }
}