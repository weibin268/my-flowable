package com.zhuang.flowable.model;

import java.util.List;

public class NextTaskInfo {
	
	private String taskKey;

	private String taskName;

	private Boolean isCountersign;

	private List<UserInfo> users;

	public String getTaskKey() {
		return taskKey;
	}

	public void setTaskKey(String taskKey) {
		this.taskKey = taskKey;
	}

	public String getTaskName() {
		
		return taskName;
	}

	public void setTaskName(String taskName) {
		
		if(taskKey.equals("_endTask_"))
		{
			this.taskName = "结束";
		}else
		{
			this.taskName = taskName;
		}
	}	
	
	public Boolean getIsCountersign() {
		return isCountersign;
	}

	public void setIsCountersign(Boolean isCountersign) {
		this.isCountersign = isCountersign;
	}

	public List<UserInfo> getUsers() {
		return users;
	}

	public void setUsers(List<UserInfo> users) {
		this.users = users;
	}
	
	
}
