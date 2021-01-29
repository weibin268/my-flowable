package com.zhuang.flowable.listener;

import com.zhuang.flowable.WorkflowEngine;
import com.zhuang.flowable.model.TaskDef;
import com.zhuang.flowable.util.ParamsUtils;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ProcessContext {

    protected String businessKey;
    protected String taskId;
    protected String comment;
    protected List<String> nextUserList;
    protected Map<String, Object> params;
    protected WorkflowEngine workflowEngine;
    protected TaskDef currentTaskDef;
    protected TaskDef nextTaskDef;
    protected String choice;

    public ProcessContext(WorkflowEngine workflowEngine) {
        this.workflowEngine = workflowEngine;
    }

    public String getBusinessData() {
        return ParamsUtils.getBusinessData(params);
    }
}
