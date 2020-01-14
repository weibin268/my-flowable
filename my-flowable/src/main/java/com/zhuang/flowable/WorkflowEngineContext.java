package com.zhuang.flowable;

import com.zhuang.flowable.model.TaskDefModel;

import java.util.List;
import java.util.Map;

public abstract class WorkflowEngineContext {
	
	protected String taskId;	
	
	protected String comment;

	protected List<String> nextUsers;
	
	protected Map<String, Object> formData;
	
	protected WorkflowEngine workflowEngine;
	
	protected TaskDefModel currentTaskDef;
	
	protected TaskDefModel nextTaskDef;
	
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

	public Map<String, Object> getFormData() {
		return formData;
	}

	public void setFormData(Map<String, Object> formData) {
		this.formData = formData;
	}

	public WorkflowEngine getWorkflowEngine() {
		return workflowEngine;
	}

	public void setWorkflowEngine(WorkflowEngine workflowEngine) {
		this.workflowEngine = workflowEngine;
	}
	
	public TaskDefModel getCurrentTaskDef() {
		return currentTaskDef;
	}

	public void setCurrentTaskDef(TaskDefModel currentTaskDef) {
		this.currentTaskDef = currentTaskDef;
	}

	public TaskDefModel getNextTaskDef() {
		return nextTaskDef;
	}

	public void setNextTaskDef(TaskDefModel nextTaskDef) {
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
