package com.zhuang.flowable.manager;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProcessVariablesManager {

    @Autowired
    private HistoryService historyService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskManager taskManager;

    public Map<String, Object> getHistoryVariablesByTaskId(String taskId) {
        Map<String, Object> result = new HashMap<String, Object>();
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery().processInstanceId(historicTaskInstance.getProcessInstanceId()).list();
        for (HistoricVariableInstance historicVariableInstance : historicVariableInstances) {
            result.put(historicVariableInstance.getVariableName(), historicVariableInstance.getValue());
        }
        return result;
    }

    public Map<String, Object> getRuntimeVariablesByTaskId(String taskId) {
        return taskService.createTaskQuery().taskId(taskId).singleResult().getProcessVariables();
    }

    public Map<String, Object> getVariablesByTaskId(String taskId) {
        return getVariablesByTaskInfo(taskManager.getTaskInfoByTaskId(taskId));
    }

    public Map<String, Object> getVariablesByTaskInfo(TaskInfo taskInfo) {
        return taskInfo.getProcessVariables();
    }
}
