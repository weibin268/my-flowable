package com.zhuang.flowable.handler;

import com.zhuang.flowable.WorkflowContext;
import com.zhuang.flowable.model.UserInfo;

import java.util.List;

public interface NextTaskUsersHandler {

    List<UserInfo> execute(WorkflowContext workflowContext);

    String key();
}
