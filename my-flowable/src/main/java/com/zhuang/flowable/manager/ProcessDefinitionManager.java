package com.zhuang.flowable.manager;


import com.zhuang.flowable.constant.EndTaskVariableNames;
import com.zhuang.flowable.exception.HistoricTaskNotFoundException;
import com.zhuang.flowable.model.TaskDef;
import com.zhuang.flowable.util.FlowableJuelUtils;
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
import org.flowable.task.api.Task;
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

    public ProcessDefinitionEntity getProcessDefinitionEntityByKey(String proDefKey) {
        ProcessDefinitionEntity def = (ProcessDefinitionEntity) repositoryService.createProcessDefinitionQuery().processDefinitionKey(proDefKey).latestVersion().singleResult();
        return def;
    }

    public TaskDef getTaskDefByTaskId(String taskId) {
        FlowNode flowNode = getFlowNodeByTaskId(taskId);
        return getTaskDefModelByFlowNode(flowNode);
    }

    public FlowNode getFlowNodeByTaskId(String taskId) {
        String taskDefinitionKey;
        String processDefinitionId;
        //先从运行时获取，没有再从历史中获取
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task != null) {
            taskDefinitionKey = task.getTaskDefinitionKey();
            processDefinitionId = task.getProcessDefinitionId();
        } else {
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
            if (historicTaskInstance == null) {
                throw new HistoricTaskNotFoundException("taskId:" + taskId);
            }
            taskDefinitionKey = historicTaskInstance.getTaskDefinitionKey();
            processDefinitionId = historicTaskInstance.getProcessDefinitionId();
        }
        Process process = repositoryService.getBpmnModel(processDefinitionId).getMainProcess();
        return (FlowNode) process.getFlowElement(taskDefinitionKey);
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
            result.setCountersign(true);
        } else {
            result.setCountersign(false);
        }
        return result;
    }

    public TaskDef getNextTaskDefByTaskId(String taskId, Map<String, Object> params) {
        FlowNode flowNode = getFlowNodeByTaskId(taskId);
        FlowNode nextFlowNode = getNextFlowNode(flowNode, params);
        return getTaskDefModelByFlowNode(nextFlowNode);
    }

    public FlowNode getNextFlowNode(FlowNode flowNode, Map<String, Object> params) {
        return getNextFlowNode(flowNode.getOutgoingFlows(), params);
    }

    public FlowNode getNextFlowNode(List<SequenceFlow> sequenceFlowList, Map<String, Object> params) {
        FlowNode result = null;
        if (sequenceFlowList.size() == 1) {
            SequenceFlow sequenceFlow = sequenceFlowList.get(0);
            FlowElement flowElement = sequenceFlow.getTargetFlowElement();
            if (flowElement instanceof Gateway) {
                Gateway gateway = (Gateway) flowElement;
                List<SequenceFlow> gatewayOutgoingFlows = gateway.getOutgoingFlows();
                result = getNextFlowNode(gatewayOutgoingFlows, params);
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
        } else {
            SequenceFlow correctGwOutTransi = null;
            SequenceFlow defaultGwOutTransi = null;
            for (SequenceFlow gwOutTransi : sequenceFlowList) {
                String conditionText = gwOutTransi.getConditionExpression();
                if (conditionText == null) {
                    defaultGwOutTransi = gwOutTransi;
                }
                if (conditionText != null && FlowableJuelUtils.evaluateBooleanResult(conditionText, params)) {
                    correctGwOutTransi = gwOutTransi;
                }
            }
            if (correctGwOutTransi == null) {
                correctGwOutTransi = defaultGwOutTransi;
            }
            if (correctGwOutTransi != null) {
                FlowElement tempFlowElement = correctGwOutTransi.getTargetFlowElement();
                if (tempFlowElement instanceof Gateway) {
                    result = getNextFlowNode((Gateway) tempFlowElement, params);
                } else if (tempFlowElement instanceof FlowNode) {
                    result = (FlowNode) tempFlowElement;
                }
            }
        }
        return result;
    }

    public List<ProcessDefinition> getProcessDefinitionList() {
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().active().latestVersion().list();
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
