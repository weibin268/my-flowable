package com.zhuang.flowable.handler;

import com.zhuang.flowable.WorkflowContext;
import com.zhuang.flowable.model.UserInfo;

import java.util.List;

public interface NextTaskUserHandler {

    List<UserInfo> execute(WorkflowContext workflowContext);

    String key();
}
