package com.zhuang.flowable;

import com.zhuang.flowable.model.TaskDef;

import java.util.List;
import java.util.Map;

public abstract class WorkflowEngineContext {
	
	protected String taskId;
	protected String comment;
	protected List<String> nextUsers;
	protected Map<String, Object> params;
	protected WorkflowEngine workflowEngine;
	protected TaskDef currentTaskDef;
	protected TaskDef nextTaskDef;
	protected String choice;	
	
	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public List<String> getNextUsers() {
		return nextUsers;
	}

	public void setNextUsers(List<String> nextUsers) {
		this.nextUsers = nextUsers;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public WorkflowEngine getWorkflowEngine() {
		return workflowEngine;
	}

	public void setWorkflowEngine(WorkflowEngine workflowEngine) {
		this.workflowEngine = workflowEngine;
	}
	
	public TaskDef getCurrentTaskDef() {
		return currentTaskDef;
	}

	public void setCurrentTaskDef(TaskDef currentTaskDef) {
		this.currentTaskDef = currentTaskDef;
	}

	public TaskDef getNextTaskDef() {
		return nextTaskDef;
	}

	public void setNextTaskDef(TaskDef nextTaskDef) {
		this.nextTaskDef = nextTaskDef;
	}

	public WorkflowEngineContext(WorkflowEngine workflowEngine)
	{
		this.workflowEngine=workflowEngine;
	}

	public String getChoice() {
		return choice;
	}

	public void setChoice(String choice) {
		this.choice = choice;
	}

	
}
