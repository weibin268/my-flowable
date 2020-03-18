package com.zhuang.flowable.manager;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.DeploymentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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

    public void deployByClasspathResource(String resourceName) {
        String resourcePath = basePath + resourceName;
        repositoryService.createDeployment().addClasspathResource(resourcePath).deploy();
    }

    public void deployByInputStream(InputStream inputStream, String resourceName) {
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        deploymentBuilder.addInputStream(resourceName, inputStream).deploy();
    }
    
    public void deleteDeployment(String deploymentId, boolean cascade) {
        repositoryService.deleteDeployment(deploymentId, cascade);
    }
}
