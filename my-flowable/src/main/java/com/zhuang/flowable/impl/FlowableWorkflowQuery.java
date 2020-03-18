package com.zhuang.flowable.impl;

import com.zhuang.flowable.WorkflowQuery;
import com.zhuang.flowable.constant.EndTaskVariableNames;
import com.zhuang.flowable.constant.ProcessMainVariableNames;
import com.zhuang.flowable.manager.ProcessDefinitionManager;
import com.zhuang.flowable.manager.ProcessInstanceManager;
import com.zhuang.flowable.manager.ProcessVariablesManager;
import com.zhuang.flowable.model.FlowInfo;
import com.zhuang.flowable.model.PageInfo;
import com.zhuang.flowable.model.ProcDef;
import com.zhuang.flowable.model.TaskInfo;
import com.zhuang.flowable.service.UserService;
import com.zhuang.flowable.util.DateUtils;
import com.zhuang.flowable.util.MapUtils;
import com.zhuang.flowable.util.VariableNameUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.task.Comment;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfoQuery;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class FlowableWorkflowQuery implements WorkflowQuery {

    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private UserService userService;
    @Autowired
    private ProcessVariablesManager processVariablesManager;
    @Autowired
    private ProcessInstanceManager processInstanceManager;
    @Autowired
    private ProcessDefinitionManager processDefinitionManager;

    @Override
    public PageInfo<FlowInfo> getMyTodoListPage(String userId, int pageNo, int pageSize, Map<String, Object> conditions) {
        //总记录查询
        TaskQuery taskQuery = taskService.createTaskQuery().taskCandidateOrAssigned(userId);
        List<FlowInfo> flowInfoList = new ArrayList<>();
        //设置查询筛选条件
        setTaskQueryConditions(taskQuery, conditions);
        //设置排序
        taskQuery.orderByTaskCreateTime().desc();
        PageInfo<FlowInfo> result = new PageInfo<FlowInfo>(pageNo, pageSize, new Long(taskQuery.count()).intValue(), flowInfoList);
        //得到分页记录
        List<Task> taskList = taskQuery.listPage(result.getPageStartRow() - 1, result.getPageSize());
        //设置流程信息实体值
        for (Task task : taskList) {
            FlowInfo flowInfo = new FlowInfo();
            flowInfo.setTaskId(task.getId());
            flowInfo.setCurrentActivityName(task.getName());
            Map<String, Object> processVariables = runtimeService.getVariables(task.getExecutionId());
            fillFlowInfoModel(flowInfo, processVariables);
            flowInfoList.add(flowInfo);
        }
        return result;
    }

    @Override
    public PageInfo<FlowInfo> getMyDoneListPage(String userId, int pageNo, int pageSize, Map<String, Object> conditions) {
        //总记录查询
        HistoricTaskInstanceQuery historicTaskInstanceQuery = historyService.createHistoricTaskInstanceQuery().taskAssignee(userId).finished();
        List<FlowInfo> flowInfoList = new ArrayList<FlowInfo>();
        //设置查询筛选条件
        setTaskQueryConditions(historicTaskInstanceQuery, conditions);
        //设置排序
        historicTaskInstanceQuery.orderByTaskCreateTime().desc();
        PageInfo<FlowInfo> result = new PageInfo<FlowInfo>(pageNo, pageSize, new Long(historicTaskInstanceQuery.count()).intValue(), flowInfoList);
        //得到分页记录
        List<HistoricTaskInstance> historicTaskInstances = historicTaskInstanceQuery.listPage(result.getPageStartRow() - 1, result.getPageSize());
        //设置流程信息实体值
        for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
            FlowInfo flowInfo = new FlowInfo();
            flowInfo.setTaskId(historicTaskInstance.getId());
            String currentActivityName = "";
            if (processInstanceManager.isProcessFinished(historicTaskInstance.getProcessInstanceId())) {
                currentActivityName = EndTaskVariableNames.NAME;
            } else {
                List<Task> tasks = taskService.createTaskQuery().processInstanceId(historicTaskInstance.getProcessInstanceId()).list();
                if (tasks.size() > 0) {
                    currentActivityName = tasks.get(0).getName();
                }
            }
            flowInfo.setCurrentActivityName(currentActivityName);
            Map<String, Object> processVariables = processVariablesManager.getProcessVariablesByTaskId(historicTaskInstance.getId());
            fillFlowInfoModel(flowInfo, processVariables);
            flowInfoList.add(flowInfo);
        }
        return result;
    }

    @Override
    public List<TaskInfo> getHistoryTaskInfoList(String taskId) {
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        String instanceId = historicTaskInstance.getProcessInstanceId();
        List<TaskInfo> taskInfos = getHistoryTaskInfoListByInstanceId(instanceId);
        return taskInfos;
    }

    @Override
    public List<ProcDef> getProcDefList() {
        List<ProcDef> procDefs = new ArrayList<ProcDef>();
        List<ProcessDefinition> processDefinitions = processDefinitionManager.getProcessDefinitionList();
        for (ProcessDefinition processDefinition : processDefinitions) {
            ProcDef procDef = new ProcDef();
            procDef.setKey(processDefinition.getKey());
            procDef.setName(processDefinition.getName());
            procDef.setCategory(processDefinition.getCategory());
            procDef.setDescription(processDefinition.getDescription());
            procDefs.add(procDef);
        }
        return procDefs;
    }

    private List<TaskInfo> getHistoryTaskInfoListByInstanceId(String instanceId) {
        List<TaskInfo> result = new ArrayList<TaskInfo>();
        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().processInstanceId(instanceId).orderByTaskCreateTime().asc().list();
        for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
            TaskInfo taskInfo = new TaskInfo();
            taskInfo.setId(historicTaskInstance.getId());
            taskInfo.setKey(historicTaskInstance.getTaskDefinitionKey());
            taskInfo.setName(historicTaskInstance.getName());
            taskInfo.setUserId(historicTaskInstance.getAssignee());
            taskInfo.setUserName(userService.getUser(taskInfo.getUserId()).getUserName());
            if (taskInfo.getUserId() == null) {
                List<String> userIds = new ArrayList<String>();
                List<String> userNames = new ArrayList<String>();
                List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskInfo.getId());
                for (IdentityLink identityLink : identityLinks) {
                    if (!identityLink.getType().equals("candidate")) {
                        continue;
                    }
                    userIds.add(identityLink.getUserId());
                    userNames.add(userService.getUser(identityLink.getUserId()).getUserName());
                }
                taskInfo.setUserId(StringUtils.join(userIds.toArray(new String[userIds.size()]), ","));
                taskInfo.setUserName(StringUtils.join(userNames.toArray(new String[userNames.size()]), ","));
            }
            taskInfo.setStartTime(historicTaskInstance.getCreateTime());
            taskInfo.setEndTime(historicTaskInstance.getEndTime());
            List<Comment> comments = taskService.getTaskComments(historicTaskInstance.getId());
            if (comments.size() > 0) {
                taskInfo.setComment(comments.get(0).getFullMessage());
            }
            result.add(taskInfo);
        }
        if (result.size() > 0) {
            TaskInfo lastTask = result.get(result.size() - 1);
            if (lastTask.getEndTime() != null) {
                boolean isEndTask = processDefinitionManager.isEndTask(lastTask.getId());
                if (isEndTask) {
                    TaskInfo taskInfo = new TaskInfo();
                    taskInfo.setId(EndTaskVariableNames.ID);
                    taskInfo.setKey(EndTaskVariableNames.KEY);
                    taskInfo.setName(EndTaskVariableNames.NAME);
                    taskInfo.setUserId(EndTaskVariableNames.USER_ID);
                    taskInfo.setUserName(EndTaskVariableNames.USER_NAME);
                    taskInfo.setStartTime(lastTask.getEndTime());
                    taskInfo.setEndTime(lastTask.getEndTime());
                    taskInfo.setComment(EndTaskVariableNames.COMMENT);
                    result.add(taskInfo);
                }
            }
        }
        return result;
    }

    private void setTaskQueryConditions(TaskInfoQuery taskInfoQuery, Map<String, Object> conditions) {
        if (conditions == null) return;
        String proDefKey = MapUtils.getString(conditions, ProcessMainVariableNames.PROC_DEF_KEY);
        if (!StringUtils.isEmpty(proDefKey)) {
            taskInfoQuery.processVariableValueEquals(ProcessMainVariableNames.PROC_DEF_KEY, proDefKey);
        }
        String proType = MapUtils.getString(conditions, ProcessMainVariableNames.PROC_TYPE);
        if (!StringUtils.isEmpty(proType)) {
            taskInfoQuery.processVariableValueEquals(ProcessMainVariableNames.PROC_TYPE, proType);
        }
        String proTitle = MapUtils.getString(conditions, ProcessMainVariableNames.PROC_TITLE);
        if (!StringUtils.isEmpty(proTitle)) {
            taskInfoQuery.processVariableValueLike(ProcessMainVariableNames.PROC_TITLE, "%" + proTitle + "%");
        }
        String proCreateUser = MapUtils.getString(conditions, ProcessMainVariableNames.PROC_CREATE_USER);
        if (!StringUtils.isEmpty(proCreateUser)) {
            taskInfoQuery.processVariableValueLike(ProcessMainVariableNames.PROC_CREATE_USER, "%" + proCreateUser + "%");
        }
        String proCreateTimeStart = MapUtils.getString(conditions, VariableNameUtils.toStartName(ProcessMainVariableNames.PROC_CREATE_TIME));
        if (!StringUtils.isEmpty(proCreateTimeStart)) {
            Date dProcCreateTimeStart = DateUtils.parseDate(proCreateTimeStart + " 00:00:00");
            taskInfoQuery.processVariableValueGreaterThanOrEqual(ProcessMainVariableNames.PROC_CREATE_TIME, dProcCreateTimeStart);
        }
        String proCreateTimeEnd = MapUtils.getString(conditions, VariableNameUtils.toEndName(ProcessMainVariableNames.PROC_CREATE_TIME));
        if (!StringUtils.isEmpty(proCreateTimeEnd)) {
            Date dProcCreateTimeEnd = DateUtils.parseDate(proCreateTimeEnd.toString() + " 23:59:59");
            taskInfoQuery.processVariableValueLessThanOrEqual(ProcessMainVariableNames.PROC_CREATE_TIME, dProcCreateTimeEnd);
        }
    }

    private void fillFlowInfoModel(FlowInfo flowInfo, Map<String, Object> processVariables) {
        flowInfo.setBusinessKey(MapUtils.getString(processVariables, ProcessMainVariableNames.PROC_BUSINESS_KEY));
        flowInfo.setDefKey(MapUtils.getString(processVariables, ProcessMainVariableNames.PROC_DEF_KEY));
        flowInfo.setTitle(MapUtils.getString(processVariables, ProcessMainVariableNames.PROC_TITLE));
        flowInfo.setType(MapUtils.getString(processVariables, ProcessMainVariableNames.PROC_TYPE));
        flowInfo.setCreateTime(MapUtils.getDate(processVariables, ProcessMainVariableNames.PROC_CREATE_TIME));
        flowInfo.setCreateUserId(MapUtils.getString(processVariables, ProcessMainVariableNames.PROC_CREATE_USER_ID));
        flowInfo.setCreateUser(MapUtils.getString(processVariables, ProcessMainVariableNames.PROC_CREATE_USER));
    }

}
