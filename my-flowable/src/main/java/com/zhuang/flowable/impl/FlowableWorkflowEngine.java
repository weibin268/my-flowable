package com.zhuang.flowable.impl;

import com.zhuang.flowable.AbstractWorkflowEngine;
import com.zhuang.flowable.WorkflowActionListener;
import com.zhuang.flowable.WorkflowEngineContext;
import com.zhuang.flowable.constant.ProcessMainVariableNames;
import com.zhuang.flowable.manager.ProcessDefinitionManager;
import com.zhuang.flowable.model.NextTaskInfo;
import com.zhuang.flowable.model.TaskDefModel;
import com.zhuang.flowable.model.UserInfo;
import com.zhuang.flowable.service.UserManagementService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FlowableWorkflowEngine extends AbstractWorkflowEngine {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private UserManagementService userManagementService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ProcessDefinitionManager processDefinitionManager;
    @Autowired(required = false)
    private List<WorkflowActionListener> workflowActionListenerList;

    @Override
    public String startNew(String processDefinitionKey, String userId, String businessKey, Map<String, Object> formData) {
        ensureFormDataNotNull(formData);
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).latestVersion().singleResult();
        formData.put(ProcessMainVariableNames.PROC_DEF_KEY, processDefinition.getKey());
        formData.put(ProcessMainVariableNames.PROC_TYPE, processDefinition.getName());
        formData.put(ProcessMainVariableNames.PROC_CREATE_TIME, new Date());
        formData.put(ProcessMainVariableNames.PROC_CREATE_USERID, userId);
        UserInfo userInfo = userManagementService.getUser(userId);
        formData.put(ProcessMainVariableNames.PROC_CREATE_USER, userInfo.getUserName());

        identityService.setAuthenticatedUserId(userId);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, formData);
        List<Task> nextTaskList = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        String firstTaskId = "";
        if (nextTaskList.size() == 1) {
            firstTaskId = nextTaskList.get(0).getId();
            taskService.setAssignee(firstTaskId, userId);
        }
        return processInstance.getId() + "|" + firstTaskId;
    }

    @Override
    public void save(String taskId, String comment, Map<String, Object> formData) {
        ensureFormDataNotNull(formData);
        taskService.setVariables(taskId, formData);
        List<String> nextUsers = new ArrayList<String>();
        WorkflowActionListener workflowActionListener = getWorkflowActionListenerByTaskId(taskId);
        WorkflowEngineContext workflowEngineContext = new FlowableWorkflowEngineContext(this);
        workflowEngineContext.setTaskId(taskId);
        workflowEngineContext.setComment(comment);
        workflowEngineContext.setNextUsers(nextUsers);
        workflowEngineContext.setFormData(formData);
        workflowEngineContext.setCurrentTaskDef(processDefinitionManager.getTaskDefModelByTaskId(taskId));
        workflowEngineContext.setNextTaskDef(getNextTaskDef(taskId, formData));
        if (workflowActionListener != null) {
            workflowActionListener.onSave(workflowEngineContext);
        }
    }

    @Override
    public void submit(String taskId, String userId, List<String> nextUsers, String comment, Map<String, Object> formData) {
        formData = ensureFormDataNotNull(formData);
        TaskDefModel currentTaskDef = processDefinitionManager.getTaskDefModelByTaskId(taskId);
        Map<String, Object> envVariables = getEnvVarFromFormData(formData);
        String choice = getChoiceFromFormData(formData);

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        if (currentTaskDef.getIsCountersign()) {
            calcCountersignVariables(taskId, envVariables, choice);
        }

        WorkflowActionListener workflowActionListener = getWorkflowActionListenerByTaskId(taskId);

        WorkflowEngineContext workflowEngineContext = new FlowableWorkflowEngineContext(this);
        workflowEngineContext.setTaskId(taskId);
        workflowEngineContext.setComment(comment);
        workflowEngineContext.setNextUsers(nextUsers);
        workflowEngineContext.setFormData(formData);
        workflowEngineContext.setCurrentTaskDef(currentTaskDef);
        workflowEngineContext.setNextTaskDef(getNextTaskDef(taskId, envVariables));
        workflowEngineContext.setChoice(choice);

        if (workflowActionListener != null) {
            workflowActionListener.beforeSubmit(workflowEngineContext);
        }

        run(task, userId, nextUsers, comment, envVariables, workflowEngineContext);

        if (workflowActionListener != null) {
            workflowActionListener.afterSubmit(workflowEngineContext);
        }

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


    private WorkflowActionListener getWorkflowActionListenerByTaskId(String taskId) {
        ProcessDefinitionEntity processDefinitionEntity = processDefinitionManager.getProcessDefinitionEntityByTaskId(taskId);
        if (workflowActionListenerList == null) return null;
        return workflowActionListenerList.stream().filter(c -> c.key().equals(processDefinitionEntity.getKey())).findFirst().orElse(null);
    }

    private TaskDefModel getNextTaskDef(String taskId, Map<String, Object> params) {
        return null;
    }

    private Map<String, Object> ensureFormDataNotNull(Map<String, Object> formData) {
        if (formData == null) {
            formData = new HashMap<>();
        }
        return formData;
    }




}
