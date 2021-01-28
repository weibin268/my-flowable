package com.zhuang.flowable.listener;


import com.zhuang.flowable.WorkflowContext;
import com.zhuang.flowable.model.UserInfo;

import java.util.List;

/**
 * 工作流动作监听接口
 * @author zwb
 *
 */
public interface WorkflowActionListener {

	/**
	 * 流程定义Key
	 * @return
	 */
	String key();

	/**
	 * 提交前调用
	 * @param context
	 */
	void beforeSubmit(WorkflowContext context);

	/**
	 * 提交后调用
	 * @param context
	 */
	void afterSubmit(WorkflowContext context);

	/**
	 * 刪除前调用
	 * @param context
	 */
	void beforeDelete(WorkflowContext context);

	/**
	 * 刪除后调用
	 * @param context
	 */
	void afterDelete(WorkflowContext context);

	/**
	 * 保存操作
	 * @param context
	 */
	void onSave(WorkflowContext context);

	/**
	 * 处理下一步处理人
	 * @param nextTaskUsers
	 */
	void onRetrieveNextTaskUsers(List<UserInfo> nextTaskUsers, WorkflowContext context);

	/***
	 * 加载表单数据
	 * @param context
	 */
	void onRetrieveParams(WorkflowContext context);


}
