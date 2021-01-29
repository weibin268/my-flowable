package com.zhuang.flowable.impl;

import com.zhuang.flowable.BaseWorkflowEngine;
import com.zhuang.flowable.enums.ProcessChoiceOptions;
import com.zhuang.flowable.handler.NextTaskUserHandler;
import com.zhuang.flowable.listener.ProcessActionListener;
import com.zhuang.flowable.listener.ProcessContext;
import com.zhuang.flowable.constant.*;
import com.zhuang.flowable.exception.HandlerNotFoundException;
import com.zhuang.flowable.manager.ProcessDefinitionManager;
import com.zhuang.flowable.manager.ProcessInstanceManager;
import com.zhuang.flowable.manager.ProcessVariablesManager;
import com.zhuang.flowable.manager.TaskManager;
import com.zhuang.flowable.model.NextTaskInfo;
import com.zhuang.flowable.model.TaskDef;
import com.zhuang.flowable.model.UserInfo;
import com.zhuang.flowable.service.UserService;
import com.zhuang.flowable.handler.MyHandlerTag;
import com.zhuang.flowable.util.ParamsUtils;
import org.flowable.engine.*;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
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
    private UserService userService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private ProcessDefinitionManager processDefinitionManager;
    @Autowired
    private ProcessInstanceManager processInstanceManager;
    @Autowired
    private TaskManager taskManager;
    @Autowired
    private ProcessVariablesManager processVariablesManager;
    @Autowired(required = false)
    private List<ProcessActionListener> processActionListenerList;
    @Autowired(required = false)
    private List<NextTaskUserHandler> nextTaskUserHandlerList;

    @Override
    public String start(String procDefKey, String userId, String businessKey, Map<String, Object> params) {
        ensureParamsNotNull(params);

        //region 构建流程变量
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(procDefKey).latestVersion().singleResult();
        params.put(ProcessMainVariableNames.PROC_BUSINESS_KEY, businessKey);
        params.put(ProcessMainVariableNames.PROC_DEF_KEY, processDefinition.getKey());
        params.put(ProcessMainVariableNames.PROC_TYPE, processDefinition.getName());
        params.put(ProcessMainVariableNames.PROC_CREATE_TIME, new Date());
        params.put(ProcessMainVariableNames.PROC_CREATE_USER_ID, userId);
        UserInfo userInfo = userService.getById(userId);
        params.put(ProcessMainVariableNames.PROC_CREATE_USER, userInfo.getUserName());
        //endregion

        //region 启动前事件
        ProcessActionListener processActionListener = getWorkflowActionListenerByProDefKey(procDefKey);
        ProcessContext processContext = new ProcessContext(this);
        processContext.setBusinessKey(businessKey);
        processContext.setTaskId(null);
        processContext.setComment(null);
        processContext.setNextUserList(new ArrayList<>());
        processContext.setParams(params);
        processContext.setCurrentTaskDef(null);
        processContext.setNextTaskDef(null);
        if (processActionListener != null) {
            processActionListener.beforeStart(processContext);
        }
        //endregion

        //region 启动流程实例
        identityService.setAuthenticatedUserId(userId);
        Map<String, Object> variables = ParamsUtils.getVariables(params);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(procDefKey, processContext.getBusinessKey(), variables);
        List<Task> nextTaskList = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        String firstTaskId = "";
        if (nextTaskList.size() == 1) {
            firstTaskId = nextTaskList.get(0).getId();
            taskService.setAssignee(firstTaskId, userId);
        }
        //endregion

        //region 启动后事件
        if (processActionListener != null) {
            processContext.setTaskId(firstTaskId);
            processActionListener.afterStart(processContext);
        }
        //endregion

        return processInstance.getId() + ":" + firstTaskId;
    }

    @Override
    public void save(String taskId, String comment, Map<String, Object> params) {
        ensureParamsNotNull(params);
        List<String> nextUserList = new ArrayList<>();
        TaskInfo taskInfo = taskManager.getTaskInfoByTaskId(taskId);
        //region 保存前事件
        ProcessActionListener processActionListener = getWorkflowActionListenerByTaskInfo(taskInfo);
        ProcessContext processContext = new ProcessContext(this);
        processContext.setBusinessKey(taskInfo.getProcessVariables().get(ProcessMainVariableNames.PROC_BUSINESS_KEY).toString());
        processContext.setTaskId(taskId);
        processContext.setComment(comment);
        processContext.setNextUserList(nextUserList);
        processContext.setParams(params);
        processContext.setCurrentTaskDef(processDefinitionManager.getTaskDefByTaskInfo(taskInfo));
        processContext.setNextTaskDef(processDefinitionManager.getNextTaskDefByTaskInfo(taskInfo, params));
        if (processActionListener != null) {
            processActionListener.beforeSave(processContext);
        }
        //endregion

        //region 保存操作
        Map<String, Object> variables = ParamsUtils.getVariables(params);
        taskService.setVariables(taskId, variables);
        //endregion

        //region 保存后事件
        if (processActionListener != null) {
            processActionListener.afterSave(processContext);
        }
        //endregion
    }

    @Override
    public void submit(String taskId, String userId, List<String> nextUserList, String comment, Map<String, Object> params) {
        params = ensureParamsNotNull(params);
        TaskInfo taskInfo = taskManager.getTaskInfoByTaskId(taskId);
        TaskDef currentTaskDef = processDefinitionManager.getTaskDefByTaskInfo(taskInfo);
        String choice = ParamsUtils.getChoice(params);
        if (currentTaskDef.isCountersign()) {
            calcCountersignVariables(taskInfo, params, choice);
        }

        //region 提交前事件
        ProcessActionListener processActionListener = getWorkflowActionListenerByTaskInfo(taskInfo);
        ProcessContext processContext = new ProcessContext(this);
        processContext.setTaskId(taskId);
        processContext.setComment(comment);
        processContext.setNextUserList(nextUserList);
        processContext.setParams(params);
        processContext.setCurrentTaskDef(currentTaskDef);
        processContext.setNextTaskDef(processDefinitionManager.getNextTaskDefByTaskInfo(taskInfo, params));
        processContext.setChoice(choice);
        if (processActionListener != null) {
            processActionListener.beforeSubmit(processContext);
        }
        //endregion

        //region 运行流程
        Map<String, Object> variables = ParamsUtils.getVariables(params);
        run(taskInfo, userId, nextUserList, comment, variables, processContext);
        //endregion

        //region 提交后事件
        if (processActionListener != null) {
            processActionListener.afterSubmit(processContext);
        }
        //endregion

    }

    @Override
    public void delete(String taskId, String comment, Map<String, Object> params) {
        params = ensureParamsNotNull(params);
        TaskInfo taskInfo = taskManager.getTaskInfoByTaskId(taskId);
        //region 删除前事件
        ProcessActionListener processActionListener = getWorkflowActionListenerByTaskInfo(taskInfo);
        ProcessContext processContext = new ProcessContext(this);
        processContext.setTaskId(taskId);
        processContext.setComment(comment);
        processContext.setParams(params);
        processContext.setCurrentTaskDef(processDefinitionManager.getTaskDefByTaskInfo(taskInfo));
        processContext.setChoice(ParamsUtils.getChoice(params));
        if (processActionListener != null) {
            processActionListener.beforeDelete(processContext);
        }
        //endregion

        //region 删除流程实例
        processInstanceManager.deleteProcessInstanceByTaskInfo(taskInfo, comment);
        //endregion

        //region 删除后事件
        if (processActionListener != null) {
            processActionListener.afterDelete(processContext);
        }
        //endregion

    }

    @Override
    public NextTaskInfo retrieveNextTaskInfo(String taskId, Map<String, Object> params) {
        NextTaskInfo result = new NextTaskInfo();
        List<UserInfo> userInfoList = new ArrayList<UserInfo>();
        String choice = ParamsUtils.getChoice(params);

        TaskInfo taskInfo = taskManager.getTaskInfoByTaskId(taskId);
        TaskDef currentTaskDef = processDefinitionManager.getTaskDefByTaskInfo(taskInfo);

        if (currentTaskDef.isCountersign()) {
            calcCountersignVariables(taskInfo, params, choice);
        }

        TaskDef nextTaskDef = processDefinitionManager.getNextTaskDefByTaskInfo(taskInfo, params);
        result.setTaskKey(nextTaskDef.getKey());
        result.setTaskName(nextTaskDef.getName());

        ProcessContext processContext = new ProcessContext(this);
        processContext.setTaskId(taskId);
        processContext.setParams(params);
        processContext.setCurrentTaskDef(currentTaskDef);
        processContext.setNextTaskDef(nextTaskDef);
        processContext.setChoice(choice);
        initNextTaskUser(userInfoList, taskId, processContext);
        String nextTaskUsersConfig;
        if (nextTaskDef.isCountersign()) {
            nextTaskUsersConfig = nextTaskDef.getCandidateUser();
        } else {
            nextTaskUsersConfig = nextTaskDef.getAssignee();
        }
        if (MyHandlerTag.isMyHandlerTag(nextTaskUsersConfig)) {
            MyHandlerTag myHandlerTag = new MyHandlerTag(nextTaskUsersConfig);
            NextTaskUserHandler nextTaskUserHandler = getNextTaskUsersHandlerByKey(myHandlerTag.getKey());
            if (nextTaskUserHandler == null) {
                throw new HandlerNotFoundException("在“nextTaskUserHandlers”中找不到key为“" + myHandlerTag.getKey() + "”的NextTaskUserHandler！");
            } else {
                processContext.setComment(myHandlerTag.getValue());
                userInfoList.addAll(nextTaskUserHandler.execute(processContext));
            }
        }
        ProcessActionListener processActionListener = getWorkflowActionListenerByTaskInfo(taskInfo);
        if (processActionListener != null) {
            processActionListener.onRetrieveNextTaskUsers(userInfoList, processContext);
        }
        result.setIsCountersign(nextTaskDef.isCountersign());
        result.setUserList(userInfoList);
        return result;
    }

    @Override
    public Map<String, Object> retrieveParams(String taskId) {
        TaskInfo taskInfo = taskManager.getTaskInfoByTaskId(taskId);
        Map<String, Object> params = processVariablesManager.getVariablesByTaskInfo(taskInfo);
        TaskDef currentTaskDef = processDefinitionManager.getTaskDefByTaskInfo(taskInfo);
        ProcessDefinition processDefinition = processDefinitionManager.getProcessDefinitionEntityByTaskInfo(taskInfo);

        params.put(ParamsVariableNames.IS_FIRST_TASK, processDefinitionManager.isFirstTask(taskInfo));
        params.put(ParamsVariableNames.CURRENT_TASK_KEY, currentTaskDef.getKey());
        params.put(ParamsVariableNames.CURRENT_TASK_NAME, currentTaskDef.getName());
        params.put(ParamsVariableNames.IS_RUNNING_TASK, taskManager.isRunningTask(taskId));
        params.put(ParamsVariableNames.PRO_DEF_KEY, processDefinition.getKey());
        params.put(ParamsVariableNames.PRO_DEF_NAME, processDefinition.getName());

        ProcessActionListener processActionListener = getWorkflowActionListenerByTaskInfo(taskInfo);
        if (processActionListener != null) {
            ProcessContext processContext = new ProcessContext(this);
            processContext.setTaskId(taskId);
            processContext.setParams(params);
            processContext.setCurrentTaskDef(currentTaskDef);
            processActionListener.onRetrieveParams(processContext);
        }

        return params;
    }

    private ProcessActionListener getWorkflowActionListenerByTaskInfo(TaskInfo taskInfo) {
        ProcessDefinitionEntity processDefinitionEntity = processDefinitionManager.getProcessDefinitionEntityByTaskInfo(taskInfo);
        if (processActionListenerList == null) return null;
        return getWorkflowActionListenerByProDefKey(processDefinitionEntity.getKey());
    }

    private ProcessActionListener getWorkflowActionListenerByProDefKey(String proDefKey) {
        return processActionListenerList.stream().filter(c -> c.key().equals(proDefKey)).findFirst().orElse(null);
    }

    private NextTaskUserHandler getNextTaskUsersHandlerByKey(String key) {
        if (nextTaskUserHandlerList == null) return null;
        return nextTaskUserHandlerList.stream().filter(c -> c.key().equals(key)).findFirst().orElse(null);
    }

    private Map<String, Object> ensureParamsNotNull(Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        return params;
    }

    private void calcCountersignVariables(TaskInfo taskInfo, Map<String, Object> envVariables, String choice) {
        Object objCountersignApprovedCount = runtimeService.getVariable(taskInfo.getProcessInstanceId(), CountersignVariableNames.COUNTERSIGN_APPROVED_COUNT);
        Integer countersignApprovedCount = null;
        if (objCountersignApprovedCount == null) {
            countersignApprovedCount = 0;
        } else {
            countersignApprovedCount = (Integer) objCountersignApprovedCount;
        }

        Object objCountersignRejectedCount = runtimeService.getVariable(taskInfo.getProcessInstanceId(), CountersignVariableNames.COUNTERSIGN_REJECTED_COUNT);
        Integer countersignRejectedCount = null;
        if (objCountersignRejectedCount == null) {
            countersignRejectedCount = 0;
        } else {
            countersignRejectedCount = (Integer) objCountersignRejectedCount;
        }

        if (ProcessChoiceOptions.APPROVE.equals(choice)) {
            ++countersignApprovedCount;
        } else if (ProcessChoiceOptions.REJECT.equals(choice)) {
            ++countersignRejectedCount;
        }
        envVariables.put(CountersignVariableNames.COUNTERSIGN_APPROVED_COUNT, countersignApprovedCount);
        envVariables.put(CountersignVariableNames.COUNTERSIGN_REJECTED_COUNT, countersignRejectedCount);
    }


    private void run(TaskInfo taskInfo, String userId, List<String> nextUserList, String comment, Map<String, Object> variables, ProcessContext processContext) {

        Boolean isCountersign4Next = processContext.getNextTaskDef().isCountersign();
        Boolean isCountersign4Current = processContext.getCurrentTaskDef().isCountersign();

        if (comment != null) {
            taskService.addComment(taskInfo.getId(), taskInfo.getProcessInstanceId(), comment);
        }

        if (isCountersign4Next) {
            variables.put(CountersignVariableNames.COUNTERSIGN_USERS, nextUserList);
        }

        taskService.setAssignee(taskInfo.getId(), userId);
        taskService.complete(taskInfo.getId(), variables);

        if (!(isCountersign4Next || isCountersign4Current)) {
            setTaskUser(taskInfo.getId(), nextUserList);
        }

        if (isCountersign4Current) {
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(taskInfo.getProcessInstanceId()).list();
            if (tasks.size() >= 0) {
                TaskDef newTaskDef = processDefinitionManager.getTaskDefByTaskId(tasks.get(0).getId());
                if (newTaskDef.isCountersign() == false) {
                    setTaskUser(taskInfo.getId(), nextUserList);
                }
            }
        }
    }

    private void setTaskUser(String preTaskId, List<String> userList) {
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(preTaskId).singleResult();
        if (userList != null && userList.size() != 0) {
            List<Task> nextTaskList = taskService.createTaskQuery().processInstanceId(historicTaskInstance.getProcessInstanceId()).list();
            for (Task nextTask : nextTaskList) {
                if (userList.size() == 1) {
                    taskService.setAssignee(nextTask.getId(), userList.get(0));
                } else {
                    for (String userId : userList) {
                        taskService.addCandidateUser(nextTask.getId(), userId);
                    }
                    taskService.setAssignee(nextTask.getId(), null);
                }
            }
        }
    }

    private void initNextTaskUser(List<UserInfo> userInfoList, String taskId, ProcessContext processContext) {
        if (ProcessChoiceOptions.BACK.equals(processContext.getChoice())) {
            String nextTaskUser = taskManager.getTaskAssignee(taskManager.getProcessInstanceId(taskId), processContext.getNextTaskDef().getKey());
            if (nextTaskUser != null) {
                UserInfo userInfo = userService.getById(nextTaskUser);
                userInfoList.add(userInfo);
            }
        }
    }

}
