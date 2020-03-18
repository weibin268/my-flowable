package com.zhuang.flowable.model;

public class ProcDef {
	
	private String key;
	private String name;
	private String category;
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

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@Override
	public String toString() {
		return "ProcDef{" +
				"key='" + key + '\'' +
				", name='" + name + '\'' +
				", category='" + category + '\'' +
				", description='" + description + '\'' +
				'}';
	}
}
