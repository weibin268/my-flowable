package com.zhuang.flowable.manager;

import com.zhuang.flowable.MyFlowableTestApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentManagerTest extends MyFlowableTestApplicationTest {

    @Autowired
    DeploymentManager deploymentManager;

    @Test
    void deployByClasspathResource() {
        deploymentManager.deployByClasspathResource("countersign-test.bpmn");
    }

    @Test
    void deployByInputStream() throws IOException {
        InputStream inputStream = getClass().getResource("/bpmn/test01.bpmn").openStream();
        deploymentManager.deployByInputStream(inputStream, "test01.bpmn");
    }

    @Test
    void deleteDeployment(){
        deploymentManager.deleteDeployment("e843fca5-68f4-11ea-8783-18602477cc91",false);
    }
}