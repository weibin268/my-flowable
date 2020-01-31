package com.zhuang.flowable.model;

import java.util.Date;

public class TaskInfo {

	private String id;

	private String name;
	
	private String key;
	
	private String userId;
	
	private String userName;
	
	private Date startTime;
	
	private Date endTime;
	
	private String comment;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public TaskInfo(String id) {
		this.id = id;
	}

	public TaskInfo() {
		
	}

	@Override
	public String toString() {
		return "TaskInfo [id=" + id + ", name=" + name + ", key=" + key + ", userId=" + userId + ", userName="
				+ userName + ", startTime=" + startTime + ", endTime=" + endTime + ", comment=" + comment + "]";
	}

}
