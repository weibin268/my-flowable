package com.zhuang.flowable;

import com.zhuang.flowable.model.UserInfo;

import java.util.List;

public interface NextTaskUsersHandler {

    List<UserInfo> execute(WorkflowEngineContext workflowEngineContext);

    String key();
}
