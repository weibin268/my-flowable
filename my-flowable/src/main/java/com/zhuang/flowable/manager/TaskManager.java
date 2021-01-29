package com.zhuang.flowable.manager;

import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskManager {

    @Autowired
    private TaskService taskService;

    public Task getByTaskId(String taskId) {
        return taskService.createTaskQuery().taskId(taskId).singleResult();
    }
}
