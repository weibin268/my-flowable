package com.zhuang.flowable.impl;

import com.zhuang.flowable.WorkflowEngine;
import com.zhuang.flowable.WorkflowContext;
import com.zhuang.flowable.manager.ProcessDefinitionManager;
import com.zhuang.flowable.model.TaskDef;
import org.springframework.beans.factory.annotation.Autowired;

public class FlowableWorkflowContext extends WorkflowContext {

	public FlowableWorkflowContext(WorkflowEngine workflowEngine) {
		super(workflowEngine);
	}
	
	@Override
	public TaskDef getCurrentTaskDef() {
		return currentTaskDef;
	}
	
	@Override	
	public TaskDef getNextTaskDef() {
		return nextTaskDef;
	}
}
