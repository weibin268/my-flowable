package com.zhuang.flowable.model;

import lombok.Data;

import java.util.Date;

@Data
public class FlowInfo {

	private String taskId;
	private String businessKey;
	private String title;
	private String createUser;
	private String createUserId;
	private Date createTime;
	private String currentActivityName;
	private String type;
	private String defKey;

}
