package com.zhuang.flowable.impl;

import com.zhuang.flowable.WorkflowEngine;
import com.zhuang.flowable.WorkflowEngineContext;
import com.zhuang.flowable.manager.ProcessDefinitionManager;
import com.zhuang.flowable.model.TaskDef;
import org.springframework.beans.factory.annotation.Autowired;

public class FlowableWorkflowEngineContext extends WorkflowEngineContext {

	@Autowired
	private ProcessDefinitionManager processDefinitionManager;

	public FlowableWorkflowEngineContext(WorkflowEngine workflowEngine) {
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
