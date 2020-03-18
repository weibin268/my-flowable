package com.zhuang.flowable.model;

public class TaskDef {

	private String key;
	private String name;
	private String assignee;
	private String candidateUser;
	private boolean isCountersign;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	public String getCandidateUser() {
		return candidateUser;
	}

	public void setCandidateUser(String candidateUser) {
		this.candidateUser = candidateUser;
	}

	public Boolean getIsCountersign() {
		return isCountersign;
	}

	public void setIsCountersign(Boolean isCountersign) {
		this.isCountersign = isCountersign;
	}

	@Override
	public String toString() {
		return "TaskDef{" +
				"key='" + key + '\'' +
				", name='" + name + '\'' +
				", assignee='" + assignee + '\'' +
				", candidateUser='" + candidateUser + '\'' +
				", isCountersign=" + isCountersign +
				'}';
	}
}
