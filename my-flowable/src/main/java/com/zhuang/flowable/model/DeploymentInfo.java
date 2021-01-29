package com.zhuang.flowable.model;

import lombok.Data;

import java.util.Date;

@Data
public class DeploymentInfo {

	private String deployId;
	private String deployName;
	private String deployCategory;
	private Date deployTime;
	private String procDefName;
	private String procDefKey;
	private int procDefVersion;
	private String procDefDescription;

}
