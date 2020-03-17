package com.zhuang.flowable;


import com.zhuang.flowable.model.UserInfo;

import java.util.List;

/**
 * 工作流动作监听接口
 * @author zwb
 *
 */
public interface WorkflowActionListener {

	/**
	 * 提交前调用
	 * @param context
	 */
	void beforeSubmit(WorkflowEngineContext context);
	
	/**
	 * 提交后调用
	 * @param context
	 */
	void afterSubmit(WorkflowEngineContext context);

	/**
	 * 刪除前调用
	 * @param context
	 */
	void beforeDelete(WorkflowEngineContext context);
	
	/**
	 * 刪除后调用
	 * @param context
	 */
	void afterDelete(WorkflowEngineContext context);
	
	/**
	 * 保存操作
	 * @param context
	 */
	void onSave(WorkflowEngineContext context);
	
	/**
	 * 处理下一步处理人
	 * @param nextTaskUsers
	 */
	void onRetrieveNextTaskUsers(List<UserInfo> nextTaskUsers, WorkflowEngineContext context);

	/***
	 * 加载表单数据
	 * @param context
	 */
	void onRetrieveParams(WorkflowEngineContext context);

	String key();
}
