package com.zhuang.flowable.impl;

import com.zhuang.flowable.BaseWorkflowEngine;
import com.zhuang.flowable.NextTaskUsersHandler;
import com.zhuang.flowable.WorkflowActionListener;
import com.zhuang.flowable.WorkflowEngineContext;
import com.zhuang.flowable.constant.*;
import com.zhuang.flowable.exception.HandlerNotFoundException;
import com.zhuang.flowable.manager.ProcessDefinitionManager;
import com.zhuang.flowable.manager.ProcessInstanceManager;
import com.zhuang.flowable.manager.ProcessVariablesManager;
import com.zhuang.flowable.manager.UserTaskManager;
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
public class FlowableWorkflowEngine extends BaseWorkflowEngine {

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
    @Autowired
    private ProcessInstanceManager processInstanceManager;
    @Autowired
    private UserTaskManager userTaskManager;
    @Autowired
    private ProcessVariablesManager processVariablesManager;
    @Autowired(required = false)
    private List<WorkflowActionListener> workflowActionListenerList;
    @Autowired(required = false)
    private List<NextTaskUsersHandler> nextTaskUsersHandlerList;

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
        formData = ensureFormDataNotNull(formData);

        WorkflowActionListener workflowActionListener = getWorkflowActionListenerByTaskId(taskId);

        WorkflowEngineContext workflowEngineContext = new FlowableWorkflowEngineContext(this);
        workflowEngineContext.setTaskId(taskId);
        workflowEngineContext.setComment(comment);
        workflowEngineContext.setFormData(formData);
        workflowEngineContext.setCurrentTaskDef(processDefinitionManager.getTaskDefModelByTaskId(taskId));
        workflowEngineContext.setChoice(getChoiceFromFormData(formData));

        if (workflowActionListener != null) {
            workflowActionListener.beforeDelete(workflowEngineContext);
        }

        processInstanceManager.deleteProcessInstanceByTaskId(taskId, comment);

        if (workflowActionListener != null) {
            workflowActionListener.afterDelete(workflowEngineContext);
        }
    }

    @Override
    public NextTaskInfo retrieveNextTaskInfo(String taskId, Map<String, Object> formData) {
        NextTaskInfo result = new NextTaskInfo();
        List<UserInfo> userInfoList = new ArrayList<UserInfo>();
        String choice = getChoiceFromFormData(formData);
        TaskDefModel currentTaskDef = processDefinitionManager.getTaskDefModelByTaskId(taskId);

        if (currentTaskDef.getIsCountersign()) {
            calcCountersignVariables(taskId, formData, choice);
        }

        TaskDefModel nextTaskDefModel = getNextTaskDef(taskId, formData);
        result.setTaskKey(nextTaskDefModel.getKey());
        result.setTaskName(nextTaskDefModel.getName());

        WorkflowEngineContext workflowEngineContext = new FlowableWorkflowEngineContext(this);
        workflowEngineContext.setTaskId(taskId);
        workflowEngineContext.setFormData(formData);
        workflowEngineContext.setCurrentTaskDef(currentTaskDef);
        workflowEngineContext.setNextTaskDef(nextTaskDefModel);
        workflowEngineContext.setChoice(choice);
        initNextTaskUsers(userInfoList, taskId, workflowEngineContext);
        String configValue = null;
        if (nextTaskDefModel.getIsCountersign()) {
            configValue = nextTaskDefModel.getCandidateUser();
        } else {
            configValue = nextTaskDefModel.getAssignee();
        }
        String[] arrConfigValue = configValue.split(CommonVariableNames.NAME_VALUE_SEPARATOR);
        String handlerKey = arrConfigValue[0];
        String handlerParams = arrConfigValue.length > 1 ? arrConfigValue[1] : null;

        if (handlerKey.startsWith(CommonVariableNames.HANDLER_NAME_PREFIX)) {
            handlerKey = handlerKey.replace(CommonVariableNames.HANDLER_NAME_PREFIX, "");
            NextTaskUsersHandler nextTaskUsersHandler = getNextTaskUsersHandlerByKey(handlerKey);

            if (nextTaskUsersHandler == null) {
                throw new HandlerNotFoundException("在“nextTaskUsersHandlers”中找不到key为“" + handlerKey + "”的NextTaskUsersHandler！");
            } else {
                workflowEngineContext.setComment(handlerParams);
                userInfoList.addAll(nextTaskUsersHandler.execute(workflowEngineContext));
            }
        }
        WorkflowActionListener workflowActionListener = getWorkflowActionListenerByTaskId(taskId);
        if (workflowActionListener != null) {
            workflowActionListener.onRetrieveNextTaskUsers(userInfoList, workflowEngineContext);
        }
        result.setIsCountersign(nextTaskDefModel.getIsCountersign());
        result.setUsers(userInfoList);
        return result;
    }

    @Override
    public Map<String, Object> retrieveFormData(String taskId) {
        Map<String, Object> formData = processVariablesManager.getProcessVariablesByTaskId(taskId);

        TaskDefModel currentTaskDefModel = processDefinitionManager.getTaskDefModelByTaskId(taskId);
        ProcessDefinition processDefinition = processDefinitionManager.getProcessDefinitionEntityByTaskId(taskId);

        formData.put(FormDataVariableNames.IS_FIRST_TASK, processDefinitionManager.isFirstTask(taskId));
        formData.put(FormDataVariableNames.CURRENT_TASK_KEY, currentTaskDefModel.getKey());
        formData.put(FormDataVariableNames.CURRENT_TASK_NAME, currentTaskDefModel.getName());
        formData.put(FormDataVariableNames.IS_RUNNING_TASK, userTaskManager.isRunningTask(taskId));
        formData.put(FormDataVariableNames.PRO_DEF_KEY, processDefinition.getKey());
        formData.put(FormDataVariableNames.PRO_DEF_NAME, processDefinition.getName());

        WorkflowActionListener workflowActionListener = getWorkflowActionListenerByTaskId(taskId);
        if (workflowActionListener != null) {
            WorkflowEngineContext workflowEngineContext = new FlowableWorkflowEngineContext(this);
            workflowEngineContext.setTaskId(taskId);
            workflowEngineContext.setFormData(formData);
            workflowEngineContext.setCurrentTaskDef(currentTaskDefModel);
            workflowActionListener.onRetrieveFormData(workflowEngineContext);
        }

        return formData;
    }


    private WorkflowActionListener getWorkflowActionListenerByTaskId(String taskId) {
        ProcessDefinitionEntity processDefinitionEntity = processDefinitionManager.getProcessDefinitionEntityByTaskId(taskId);
        if (workflowActionListenerList == null) return null;
        return workflowActionListenerList.stream().filter(c -> c.key().equals(processDefinitionEntity.getKey())).findFirst().orElse(null);
    }

    private NextTaskUsersHandler getNextTaskUsersHandlerByKey(String key) {
        if (nextTaskUsersHandlerList == null) return null;
        return nextTaskUsersHandlerList.stream().filter(c -> c.key().equals(key)).findFirst().orElse(null);
    }

    private TaskDefModel getNextTaskDef(String taskId, Map<String, Object> params) {
        TaskDefModel taskDefModel = new TaskDefModel();
        return taskDefModel;
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
            countersignApprovedCount = 0;
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
        Object objChoice = formData.get(WorkflowChoiceOptions.STORE_KEY);
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
                TaskDefModel newTaskDefModel = processDefinitionManager.getTaskDefModelByTaskId(tasks.get(0).getId());
                if (newTaskDefModel.getIsCountersign() == false) {
                    setTaskUser(task.getId(), nextUsers);
                }
            }
        }
    }

    private void setTaskUser(String preTaskId, List<String> users) {
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(preTaskId).singleResult();
        if (users != null && users.size() != 0) {
            List<Task> nextTaskList = taskService.createTaskQuery().processInstanceId(historicTaskInstance.getProcessInstanceId()).list();
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

    private void initNextTaskUsers(List<UserInfo> userInfos, String taskId, WorkflowEngineContext workflowEngineContext) {
        if (workflowEngineContext.getChoice().equals(WorkflowChoiceOptions.BACK)) {
            String nextTaskUser = userTaskManager.getTaskAssignee(userTaskManager.getProcessInstanceId(taskId), workflowEngineContext.getNextTaskDef().getKey());
            if (nextTaskUser != null) {
                UserInfo userInfo = userManagementService.getUser(nextTaskUser);
                userInfos.add(userInfo);
            }
        }
    }

}
