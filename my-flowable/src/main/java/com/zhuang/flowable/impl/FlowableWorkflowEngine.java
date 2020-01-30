package com.zhuang.flowable.impl;

import com.zhuang.flowable.AbstractWorkflowEngine;
import com.zhuang.flowable.WorkflowActionListener;
import com.zhuang.flowable.WorkflowEngineContext;
import com.zhuang.flowable.constant.CountersignVariableNames;
import com.zhuang.flowable.constant.ProcessMainVariableNames;
import com.zhuang.flowable.constant.WorkflowChoiceOptions;
import com.zhuang.flowable.manager.ProcessDefinitionManager;
import com.zhuang.flowable.model.NextTaskInfo;
import com.zhuang.flowable.model.TaskDefModel;
import com.zhuang.flowable.model.UserInfo;
import com.zhuang.flowable.service.UserManagementService;
import org.flowable.engine.*;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
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
    private HistoryService historyService;
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
        String choice = getChoiceFromFormData(formData);

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        if (currentTaskDef.getIsCountersign()) {
            calcCountersignVariables(taskId, formData, choice);
        }

        WorkflowActionListener workflowActionListener = getWorkflowActionListenerByTaskId(taskId);

        WorkflowEngineContext workflowEngineContext = new FlowableWorkflowEngineContext(this);
        workflowEngineContext.setTaskId(taskId);
        workflowEngineContext.setComment(comment);
        workflowEngineContext.setNextUsers(nextUsers);
        workflowEngineContext.setFormData(formData);
        workflowEngineContext.setCurrentTaskDef(currentTaskDef);
        workflowEngineContext.setNextTaskDef(getNextTaskDef(taskId, formData));
        workflowEngineContext.setChoice(choice);

        if (workflowActionListener != null) {
            workflowActionListener.beforeSubmit(workflowEngineContext);
        }

        run(task, userId, nextUsers, comment, formData, workflowEngineContext);

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

    private void calcCountersignVariables(String taskId, Map<String, Object> envVariables, String choice) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        Object objCountersignApprovedCount = runtimeService.getVariable(task.getProcessInstanceId(), CountersignVariableNames.COUNTERSIGN_APPROVED_COUNT);
        Integer countersignApprovedCount = null;
        if (objCountersignApprovedCount == null) {
            countersignApprovedCount = new Integer(0);
        } else {
            countersignApprovedCount = (Integer) objCountersignApprovedCount;
        }

        Object objCountersignRejectedCount = runtimeService.getVariable(task.getProcessInstanceId(), CountersignVariableNames.COUNTERSIGN_REJECTED_COUNT);
        Integer countersignRejectedCount = null;
        if (objCountersignRejectedCount == null) {
            countersignRejectedCount = new Integer(0);
        } else {
            countersignRejectedCount = (Integer) objCountersignRejectedCount;
        }

        if (choice.equals(WorkflowChoiceOptions.APPROVE)) {
            ++countersignApprovedCount;
        } else if (choice.equals(WorkflowChoiceOptions.REJECT)) {
            ++countersignRejectedCount;
        }

        envVariables.put(CountersignVariableNames.COUNTERSIGN_APPROVED_COUNT, countersignApprovedCount);
        envVariables.put(CountersignVariableNames.COUNTERSIGN_REJECTED_COUNT, countersignRejectedCount);

    }

    private String getChoiceFromFormData(Map<String, Object> formData) {
        Object objChoice = formData.get(WorkflowChoiceOptions.getStoreKey());
        return objChoice == null ? "" : objChoice.toString();
    }

    private void run(Task task, String userId, List<String> nextUsers, String comment, Map<String, Object> envVariables, WorkflowEngineContext workflowEngineContext) {

        Boolean isCountersign4Next = workflowEngineContext.getNextTaskDef().getIsCountersign();
        Boolean isCountersign4Current = workflowEngineContext.getCurrentTaskDef().getIsCountersign();

        if (comment != null) {
            taskService.addComment(task.getId(), task.getProcessInstanceId(), comment);
        }

        if (isCountersign4Next) {
            envVariables.put(CountersignVariableNames.COUNTERSIGN_USERS, nextUsers);
        }

        taskService.setAssignee(task.getId(), userId);
        taskService.complete(task.getId(), envVariables);

        if (!(isCountersign4Next || isCountersign4Current)) {
            setTaskUser(task.getId(), nextUsers);
        }

        if (isCountersign4Current) {
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
            if (tasks.size() >= 0) {
                TaskDefModel newTaskDefModel = processDefinitionManager.convertActivityImplToTaskDefModel(
                        processDefinitionManager.getActivityImpl(tasks.get(0).getId()));
                if (newTaskDefModel.getIsCountersign() == false) {
                    setTaskUser(task.getId(), nextUsers);
                }
            }
        }
    }

    private void setTaskUser(String preTaskId, List<String> users) {
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
                .taskId(preTaskId).singleResult();
        if (users != null && users.size() != 0) {
            List<Task> nextTaskList = taskService.createTaskQuery().processInstanceId(historicTaskInstance
                    .getProcessInstanceId()).list();
            for (Task nextTask : nextTaskList) {
                if (users.size() == 1) {
                    // nextTask.setAssignee(nextUsers.get(0));
                    taskService.setAssignee(nextTask.getId(), users.get(0));
                } else {
                    for (String userId : users) {
                        taskService.addCandidateUser(nextTask.getId(), userId);
                    }
                    taskService.setAssignee(nextTask.getId(), null);
                }
            }
        }
    }
}
