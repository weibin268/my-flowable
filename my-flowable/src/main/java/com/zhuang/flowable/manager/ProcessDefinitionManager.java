package com.zhuang.flowable.manager;


import com.zhuang.flowable.constant.EndTaskVariableNames;
import com.zhuang.flowable.exception.HistoricTaskNotFoundException;
import com.zhuang.flowable.model.TaskDef;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.impl.RepositoryServiceImpl;
import org.flowable.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;
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

    public TaskDef getTaskDefByTaskId(String taskId) {
        FlowNode flowNode = getFlowNodeByTaskId(taskId);
        return getTaskDefModelByFlowNode(flowNode);
    }

    public TaskDef getNextTaskDefByTaskId(String taskId, Map<String, Object> params) {
        FlowNode flowNode = getFlowNodeByTaskId(taskId);

        FlowNode nextFlowNode = getNextFlowNode(flowNode);
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

    public TaskDef getTaskDefModelByFlowNode(FlowNode flowNode) {
        TaskDef result = new TaskDef();
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
        if (flowNode.getBehavior() instanceof ParallelMultiInstanceBehavior) {
            result.setIsCountersign(true);
        } else {
            result.setIsCountersign(false);
        }
        return result;
    }

    public FlowNode getNextFlowNode(FlowNode flowNode) {
        FlowNode result = null;
        List<SequenceFlow> outgoingFlows = flowNode.getOutgoingFlows();
        if (outgoingFlows.size() == 1) {
            SequenceFlow sequenceFlow = outgoingFlows.get(0);
            FlowElement flowElement = sequenceFlow.getTargetFlowElement();
            if (isGatewayElement(flowElement)) {
                Gateway gateway = (Gateway) flowElement;
                List<SequenceFlow> gatewayOutgoingFlows = gateway.getOutgoingFlows();
                result = getNextActivityImpl(gatewayOutgoingTransitions, params);
            } else if (flowElement instanceof UserTask) {
                result = (UserTask) flowElement;
            } else if (flowElement instanceof EndEvent) {
                result = (EndEvent) flowElement;
                /*
                 * result=new TaskDefinition(null);
                 *
                 * result.setKey(EndTaskVariableNames.KEY);
                 *
                 * //ValueExpression
                 * valueExpression=ExpressionFactory.newInstance().
                 * createValueExpression("结束", String.class);
                 *
                 * result.setNameExpression(new JuelExpression(null,
                 * EndTaskVariableNames.NAME));
                 */
            }
        }

        return result;
    }


    public boolean isGatewayElement(FlowElement flowElement) {
        if (flowElement instanceof Gateway) {
            return true;
        } else {
            return false;
        }
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

    public boolean isFirstTask(String taskId) {
        FlowNode flowNode = getFlowNodeByTaskId(taskId);
        if (flowNode instanceof StartEvent) {
            return true;
        } else {
            return false;
        }
    }


}
