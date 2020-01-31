package com.zhuang.flowable.manager;


import com.zhuang.flowable.constant.EndTaskVariableNames;
import com.zhuang.flowable.exception.HistoricTaskNotFoundException;
import com.zhuang.flowable.model.TaskDefModel;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.impl.RepositoryServiceImpl;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.flowable.engine.impl.util.ProcessDefinitionUtil;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ProcessDefinitionManager {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    public ProcessDefinitionEntity getProcessDefinitionEntityByTaskId(String taskId) {
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        ProcessDefinitionEntity def = (ProcessDefinitionEntity) (((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(historicTaskInstance.getProcessDefinitionId()));
        return def;
    }

    public ProcessDefinitionEntity getProcessDefinitionEntityByKey(String proDefkey) {
        ProcessDefinitionEntity def = (ProcessDefinitionEntity) repositoryService.createProcessDefinitionQuery().processDefinitionKey(proDefkey).latestVersion().singleResult();
        return def;
    }

    public TaskDefModel getTaskDefModelByTaskId(String taskId) {
        FlowNode flowNode = getFlowNodeByTaskId(taskId);
        return getTaskDefModelByFlowNode(flowNode);
    }

    public FlowNode getFlowNodeByTaskId(String taskId) {
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        if (historicTaskInstance == null) {
            throw new HistoricTaskNotFoundException("taskId:" + taskId);
        }
        Process process = repositoryService.getBpmnModel(historicTaskInstance.getProcessDefinitionId()).getMainProcess();
        return (FlowNode) process.getFlowElement(historicTaskInstance.getTaskDefinitionKey());
    }

    public TaskDefModel getTaskDefModelByFlowNode(FlowNode flowNode) {
        TaskDefModel result = new TaskDefModel();
        if (flowNode instanceof EndEvent) {
            result.setKey(EndTaskVariableNames.KEY);
            result.setName(EndTaskVariableNames.NAME);
            result.setAssignee("");
            result.setCandidateUser("");
        } else {
            result.setKey(flowNode.getId());
            result.setName(flowNode.getName());
            if (flowNode instanceof UserTask) {
                UserTask userTask = (UserTask) flowNode;
                result.setAssignee(userTask.getAssignee());
                for (String candidateUser : userTask.getCandidateUsers()) {
                    result.setCandidateUser(candidateUser);
                    break;
                }
            }
        }
        return result;
    }

    public List<ProcessDefinition> getProcessDefinitionList() {
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().active()
                .latestVersion().list();
        return processDefinitions;
    }

    public boolean isEndTask(String taskId) {
        FlowNode flowNode = getFlowNodeByTaskId(taskId);
        if (flowNode instanceof EndEvent) {
            return true;
        } else {
            return false;
        }
    }

}
