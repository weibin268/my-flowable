package com.zhuang.flowable.manager;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessInstanceManager {

    @Autowired
    HistoryService historyService;


    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    public String getApplyUserId(String taskId) {

        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();

        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService
                .createHistoricProcessInstanceQuery();
        HistoricProcessInstance historicProcessInstance = historicProcessInstanceQuery
                .processInstanceId(historicTaskInstance.getProcessInstanceId()).singleResult();

        return historicProcessInstance.getStartUserId();

    }

    public void deleteProcessInstanceByTaskId(String taskId, String deleteReason) {
        String processInstanceId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
        runtimeService.deleteProcessInstance(processInstanceId, deleteReason);
    }

    public boolean isProcessFinished(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        return processInstance == null ? true : false;

    }
}
