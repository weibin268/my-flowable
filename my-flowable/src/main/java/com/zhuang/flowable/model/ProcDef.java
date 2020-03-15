package com.zhuang.flowable.model;

public class ProcDef {
	
	private String key;
	private String name;
	private String description;
	
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
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "ProcDefModel [key=" + key + ", name=" + name + ", description=" + description + "]";
	}
	
}
