package com.zhuang.flowable.model;

import java.util.Date;

public class DeploymentInfo {

	private String deployId;
	private String deployName;
	private String deployCategory;
	private Date deployTime;
	private String procDefName;
	private String procDefKey;
	private int procDefVersion;
	private String procDefDescription;

	public Date getDeployTime() {
		return deployTime;
	}

	public String getDeployId() {
		return deployId;
	}

	public void setDeployId(String deployId) {
		this.deployId = deployId;
	}

	public String getDeployName() {
		return deployName;
	}

	public void setDeployName(String deployName) {
		this.deployName = deployName;
	}

	public String getDeployCategory() {
		return deployCategory;
	}

	public void setDeployCategory(String deployCategory) {
		this.deployCategory = deployCategory;
	}

	public void setDeployTime(Date deployTime) {
		this.deployTime = deployTime;
	}

	public String getProcDefName() {
		return procDefName;
	}

	public void setProcDefName(String procDefName) {
		this.procDefName = procDefName;
	}

	public String getProcDefKey() {
		return procDefKey;
	}

	public void setProcDefKey(String procDefKey) {
		this.procDefKey = procDefKey;
	}

	public int getProcDefVersion() {
		return procDefVersion;
	}

	public void setProcDefVersion(int procDefVersion) {
		this.procDefVersion = procDefVersion;
	}

	public String getProcDefDescription() {
		return procDefDescription;
	}

	public void setProcDefDescription(String procDefDescription) {
		this.procDefDescription = procDefDescription;
	}

}
