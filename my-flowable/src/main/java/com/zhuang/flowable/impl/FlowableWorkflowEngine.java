package com.zhuang.flowable.impl;

import com.zhuang.flowable.BaseWorkflowEngine;
import com.zhuang.flowable.NextTaskUsersHandler;
import com.zhuang.flowable.WorkflowActionListener;
import com.zhuang.flowable.WorkflowContext;
import com.zhuang.flowable.constant.*;
import com.zhuang.flowable.exception.HandlerNotFoundException;
import com.zhuang.flowable.manager.ProcessDefinitionManager;
import com.zhuang.flowable.manager.ProcessInstanceManager;
import com.zhuang.flowable.manager.ProcessVariablesManager;
import com.zhuang.flowable.manager.UserTaskManager;
import com.zhuang.flowable.model.NextTaskInfo;
import com.zhuang.flowable.model.TaskDef;
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
    public String startNew(String processDefinitionKey, String userId, String businessKey, Map<String, Object> params) {
        ensureParamsNotNull(params);
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).latestVersion().singleResult();
        params.put(ProcessMainVariableNames.PROC_BUSINESS_KEY, businessKey);
        params.put(ProcessMainVariableNames.PROC_DEF_KEY, processDefinition.getKey());
        params.put(ProcessMainVariableNames.PROC_TYPE, processDefinition.getName());
        params.put(ProcessMainVariableNames.PROC_CREATE_TIME, new Date());
        params.put(ProcessMainVariableNames.PROC_CREATE_USER_ID, userId);
        UserInfo userInfo = userManagementService.getUser(userId);
        params.put(ProcessMainVariableNames.PROC_CREATE_USER, userInfo.getUserName());

        identityService.setAuthenticatedUserId(userId);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, params);
        List<Task> nextTaskList = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        String firstTaskId = "";
        if (nextTaskList.size() == 1) {
            firstTaskId = nextTaskList.get(0).getId();
            taskService.setAssignee(firstTaskId, userId);
        }
        return processInstance.getId() + "|" + firstTaskId;
    }

    @Override
    public void save(String taskId, String comment, Map<String, Object> params) {
        ensureParamsNotNull(params);
        taskService.setVariables(taskId, params);
        List<String> nextUsers = new ArrayList<String>();
        WorkflowActionListener workflowActionListener = getWorkflowActionListenerByTaskId(taskId);
        WorkflowContext workflowContext = new FlowableWorkflowContext(this);
        workflowContext.setTaskId(taskId);
        workflowContext.setComment(comment);
        workflowContext.setNextUsers(nextUsers);
        workflowContext.setParams(params);
        workflowContext.setCurrentTaskDef(processDefinitionManager.getTaskDefByTaskId(taskId));
        workflowContext.setNextTaskDef(getNextTaskDef(taskId, params));
        if (workflowActionListener != null) {
            workflowActionListener.onSave(workflowContext);
        }
    }

    @Override
    public void submit(String taskId, String userId, List<String> nextUsers, String comment, Map<String, Object> params) {
        params = ensureParamsNotNull(params);
        TaskDef currentTaskDef = processDefinitionManager.getTaskDefByTaskId(taskId);
        String choice = getChoiceFromParams(params);

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        if (currentTaskDef.getIsCountersign()) {
            calcCountersignVariables(taskId, params, choice);
        }

        WorkflowActionListener workflowActionListener = getWorkflowActionListenerByTaskId(taskId);

        WorkflowContext workflowContext = new FlowableWorkflowContext(this);
        workflowContext.setTaskId(taskId);
        workflowContext.setComment(comment);
        workflowContext.setNextUsers(nextUsers);
        workflowContext.setParams(params);
        workflowContext.setCurrentTaskDef(currentTaskDef);
        workflowContext.setNextTaskDef(getNextTaskDef(taskId, params));
        workflowContext.setChoice(choice);

        if (workflowActionListener != null) {
            workflowActionListener.beforeSubmit(workflowContext);
        }

        run(task, userId, nextUsers, comment, params, workflowContext);

        if (workflowActionListener != null) {
            workflowActionListener.afterSubmit(workflowContext);
        }

    }

    @Override
    public void delete(String taskId, String comment, Map<String, Object> params) {
        params = ensureParamsNotNull(params);

        WorkflowActionListener workflowActionListener = getWorkflowActionListenerByTaskId(taskId);

        WorkflowContext workflowContext = new FlowableWorkflowContext(this);
        workflowContext.setTaskId(taskId);
        workflowContext.setComment(comment);
        workflowContext.setParams(params);
        workflowContext.setCurrentTaskDef(processDefinitionManager.getTaskDefByTaskId(taskId));
        workflowContext.setChoice(getChoiceFromParams(params));

        if (workflowActionListener != null) {
            workflowActionListener.beforeDelete(workflowContext);
        }

        processInstanceManager.deleteProcessInstanceByTaskId(taskId, comment);

        if (workflowActionListener != null) {
            workflowActionListener.afterDelete(workflowContext);
        }
    }

    @Override
    public NextTaskInfo retrieveNextTaskInfo(String taskId, Map<String, Object> params) {
        NextTaskInfo result = new NextTaskInfo();
        List<UserInfo> userInfoList = new ArrayList<UserInfo>();
        String choice = getChoiceFromParams(params);
        TaskDef currentTaskDef = processDefinitionManager.getTaskDefByTaskId(taskId);

        if (currentTaskDef.getIsCountersign()) {
            calcCountersignVariables(taskId, params, choice);
        }

        TaskDef nextTaskDef = getNextTaskDef(taskId, params);
        result.setTaskKey(nextTaskDef.getKey());
        result.setTaskName(nextTaskDef.getName());

        WorkflowContext workflowContext = new FlowableWorkflowContext(this);
        workflowContext.setTaskId(taskId);
        workflowContext.setParams(params);
        workflowContext.setCurrentTaskDef(currentTaskDef);
        workflowContext.setNextTaskDef(nextTaskDef);
        workflowContext.setChoice(choice);
        initNextTaskUsers(userInfoList, taskId, workflowContext);
        String configValue = null;
        if (nextTaskDef.getIsCountersign()) {
            configValue = nextTaskDef.getCandidateUser();
        } else {
            configValue = nextTaskDef.getAssignee();
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
                workflowContext.setComment(handlerParams);
                userInfoList.addAll(nextTaskUsersHandler.execute(workflowContext));
            }
        }
        WorkflowActionListener workflowActionListener = getWorkflowActionListenerByTaskId(taskId);
        if (workflowActionListener != null) {
            workflowActionListener.onRetrieveNextTaskUsers(userInfoList, workflowContext);
        }
        result.setIsCountersign(nextTaskDef.getIsCountersign());
        result.setUsers(userInfoList);
        return result;
    }

    @Override
    public Map<String, Object> retrieveParams(String taskId) {
        Map<String, Object> params = processVariablesManager.getProcessVariablesByTaskId(taskId);

        TaskDef currentTaskDef = processDefinitionManager.getTaskDefByTaskId(taskId);
        ProcessDefinition processDefinition = processDefinitionManager.getProcessDefinitionEntityByTaskId(taskId);

        params.put(ParamsVariableNames.IS_FIRST_TASK, processDefinitionManager.isFirstTask(taskId));
        params.put(ParamsVariableNames.CURRENT_TASK_KEY, currentTaskDef.getKey());
        params.put(ParamsVariableNames.CURRENT_TASK_NAME, currentTaskDef.getName());
        params.put(ParamsVariableNames.IS_RUNNING_TASK, userTaskManager.isRunningTask(taskId));
        params.put(ParamsVariableNames.PRO_DEF_KEY, processDefinition.getKey());
        params.put(ParamsVariableNames.PRO_DEF_NAME, processDefinition.getName());

        WorkflowActionListener workflowActionListener = getWorkflowActionListenerByTaskId(taskId);
        if (workflowActionListener != null) {
            WorkflowContext workflowContext = new FlowableWorkflowContext(this);
            workflowContext.setTaskId(taskId);
            workflowContext.setParams(params);
            workflowContext.setCurrentTaskDef(currentTaskDef);
            workflowActionListener.onRetrieveParams(workflowContext);
        }

        return params;
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

    private TaskDef getNextTaskDef(String taskId, Map<String, Object> params) {
        TaskDef taskDef = processDefinitionManager.getNextTaskDefByTaskId(taskId, params);
        return taskDef;
    }

    private Map<String, Object> ensureParamsNotNull(Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        return params;
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

    private String getChoiceFromParams(Map<String, Object> params) {
        Object objChoice = params.get(WorkflowChoiceOptions.STORE_KEY);
        return objChoice == null ? "" : objChoice.toString();
    }

    private void run(Task task, String userId, List<String> nextUsers, String comment, Map<String, Object> envVariables, WorkflowContext workflowContext) {

        Boolean isCountersign4Next = workflowContext.getNextTaskDef().getIsCountersign();
        Boolean isCountersign4Current = workflowContext.getCurrentTaskDef().getIsCountersign();

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
                TaskDef newTaskDef = processDefinitionManager.getTaskDefByTaskId(tasks.get(0).getId());
                if (newTaskDef.getIsCountersign() == false) {
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

    private void initNextTaskUsers(List<UserInfo> userInfos, String taskId, WorkflowContext workflowContext) {
        if (workflowContext.getChoice().equals(WorkflowChoiceOptions.BACK)) {
            String nextTaskUser = userTaskManager.getTaskAssignee(userTaskManager.getProcessInstanceId(taskId), workflowContext.getNextTaskDef().getKey());
            if (nextTaskUser != null) {
                UserInfo userInfo = userManagementService.getUser(nextTaskUser);
                userInfos.add(userInfo);
            }
        }
    }

}
