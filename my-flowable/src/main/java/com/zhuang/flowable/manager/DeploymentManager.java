package com.zhuang.flowable.manager;

import org.flowable.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class DeploymentManager {

    private String basePath = "bpmn/";

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @Autowired
    private RepositoryService repositoryService;

    public void deployByClasspathResource(String resourceName, String deployName) {
        String resourcePath = basePath + resourceName;
        repositoryService.createDeployment().name(deployName).addClasspathResource(resourcePath).deploy();
    }

    public void deployByInputStream(String resourceName, InputStream inputStream) {
        repositoryService.createDeployment().name(resourceName).addInputStream(resourceName, inputStream).deploy();
    }
}
