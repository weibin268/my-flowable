package com.zhuang.flowable.impl;

import com.zhuang.flowable.WorkflowEngine;
import com.zhuang.flowable.WorkflowEngineContext;
import com.zhuang.flowable.manager.ProcessDefinitionManager;
import com.zhuang.flowable.model.TaskDefModel;
import org.springframework.beans.factory.annotation.Autowired;

public class FlowableWorkflowEngineContext extends WorkflowEngineContext {

	@Autowired
	private ProcessDefinitionManager processDefinitionManager;

	public FlowableWorkflowEngineContext(WorkflowEngine workflowEngine) {
		super(workflowEngine);
	}
	
	@Override
	public TaskDefModel getCurrentTaskDef() {
		
		/*ActivitiWorkflowEngine activitiWorkflowEngine = (ActivitiWorkflowEngine) workflowEngine;
		Task task = activitiWorkflowEngine.getTaskService().createTaskQuery().taskId(currentTask.getId())
				.singleResult();
		
		currentTask.setName(task.getName());*/
		
		return currentTaskDef;
	}
	
	@Override	
	public TaskDefModel getNextTaskDef() {
		
		FlowableWorkflowEngine activitiWorkflowEngine = (FlowableWorkflowEngine) workflowEngine;
		
//		Map<String, Object> params=activitiWorkflowEngine.getEnvVarFromFormData(formData);
//		Task nextTask = processDefinitionManager.getNextTaskDefinition(currentTask.getId(), params);
		
		return nextTaskDef;
	}
}
