package com.zhuang.flowable.listener;


import com.zhuang.flowable.model.UserInfo;

import java.util.List;

/**
 * 工作流动作监听接口
 * @author zwb
 *
 */
public interface ProcessActionListener {

	/**
	 * 流程定义Key
	 * @return
	 */
	String key();

	/**
	 * 启动前调用
	 * @param context
	 */
	void beforeStart(ProcessContext context);

	/**
	 * 启动后调用
	 * @param context
	 */
	void afterStart(ProcessContext context);

	/**
	 * 保存前调用
	 * @param context
	 */
	void beforeSave(ProcessContext context);

	/**
	 * 保存后调用
	 * @param context
	 */
	void afterSave(ProcessContext context);

	/**
	 * 提交前调用
	 * @param context
	 */
	void beforeSubmit(ProcessContext context);

	/**
	 * 提交后调用
	 * @param context
	 */
	void afterSubmit(ProcessContext context);

	/**
	 * 刪除前调用
	 * @param context
	 */
	void beforeDelete(ProcessContext context);

	/**
	 * 刪除后调用
	 * @param context
	 */
	void afterDelete(ProcessContext context);


	/**
	 * 处理下一步处理人
	 * @param nextTaskUsers
	 */
	void onRetrieveNextTaskUsers(List<UserInfo> nextTaskUsers, ProcessContext context);

	/***
	 * 加载表单数据
	 * @param context
	 */
	void onRetrieveParams(ProcessContext context);


}
