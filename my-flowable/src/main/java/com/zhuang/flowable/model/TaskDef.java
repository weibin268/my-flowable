package com.zhuang.flowable.model;

import lombok.Data;

@Data
public class TaskDef {

	private String key;
	private String name;
	private String assignee;
	private String candidateUser;
	private boolean isCountersign;


}
