package com.zhuang.flowable;

import com.zhuang.flowable.model.FlowInfo;
import com.zhuang.flowable.model.PageInfo;
import com.zhuang.flowable.model.ProcDefModel;
import com.zhuang.flowable.model.TaskInfo;

import java.util.List;
import java.util.Map;

/**
 * 工作流查询管理器接口
 * 
 * @author zwb
 *
 */
public interface WorkflowQuery {

	/**
	 * 我的待办分页查询
	 * 
	 * @param userId
	 *            用户ID
	 * @param pageNo
	 *            第几页
	 * @param pageSize
	 *            一页多少记录
	 * @param conditions
	 *            查询筛选条件
	 * @return
	 */
	PageInfo<FlowInfo> getMyTodoListPage(String userId, int pageNo, int pageSize, Map<String, Object> conditions);

	/**
	 * 我的已办分页查询
	 * 
	 * @param userId
	 *            用户ID
	 * @param pageNo
	 *            第几页
	 * @param pageSize
	 *            一页多少记录
	 * @param conditions
	 *            查询筛选条件
	 * @return
	 */
	PageInfo<FlowInfo> getMyDoneListPage(String userId, int pageNo, int pageSize, Map<String, Object> conditions);

	/**
	 * 获取历史审批任务信息列表
	 * @param instanceId
	 * @return
	 */
	List<TaskInfo> getHistoryTaskInfoList(String instanceId);

	/***
	 * 获取流程定义信息列表
	 * @return
	 */
	List<ProcDefModel> getProcDefList();
	
}
