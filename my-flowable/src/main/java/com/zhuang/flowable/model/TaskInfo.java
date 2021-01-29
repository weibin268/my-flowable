package com.zhuang.flowable.model;

import lombok.Data;

import java.util.Date;

@Data
public class TaskInfo {

	private String id;
	private String name;
	private String key;
	private String userId;
	private String userName;
	private Date startTime;
	private Date endTime;
	private String comment;

}
