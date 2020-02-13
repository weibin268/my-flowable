package com.zhuang.flowable.manager;

import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserTaskManager {

    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;

    public boolean canExecuteTask(String taskId, String userId) {
        TaskQuery taskQuery = taskService.createTaskQuery().taskId(taskId).taskCandidateOrAssigned(userId);
        long count = taskQuery.count();
        return count > 0 ? true : false;
    }

    public boolean isRunningTask(String taskId) {
        TaskQuery taskQuery = taskService.createTaskQuery().taskId(taskId);
        long count = taskQuery.count();
        return count > 0 ? true : false;
    }

    public String getTaskAssignee(String processInstanceId, String taskDefinitionKey) {
        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId).taskDefinitionKey(taskDefinitionKey).list();
        if (!historicTaskInstances.isEmpty()) {
            return historicTaskInstances.get(0).getAssignee();

        } else {
            return null;
        }
    }

    public String getProcessInstanceId(String taskId) {
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        return historicTaskInstance.getProcessInstanceId();
    }
}
