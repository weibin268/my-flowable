package com.zhuang.flowable.manager;

import com.zhuang.flowable.exception.HistoricTaskNotFoundException;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskManager {

    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;

    public Task getTaskByTaskId(String taskId) {
        return taskService.createTaskQuery().taskId(taskId).singleResult();
    }

    public HistoricTaskInstance getHistoricTaskInstanceByTaskId(String taskId) {
        return historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
    }

    /**
     * 先从运行时获取，没有再从历史中获取
     * @param taskId
     * @return
     */
    public TaskInfo getTaskInfoByTaskId(String taskId) {
        Task task = getTaskByTaskId(taskId);
        if (task != null) return task;
        return getHistoricTaskInstanceByTaskId(taskId);
    }
}
