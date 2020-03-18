package com.zhuang.flowable.handler;

import com.zhuang.flowable.handler.NextTaskUsersHandler;
import com.zhuang.flowable.WorkflowContext;
import com.zhuang.flowable.model.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class SampleNextTaskUserHandler implements NextTaskUsersHandler {

    @Override
    public List<UserInfo> execute(WorkflowContext workflowContext) {
        List<UserInfo> result = new ArrayList<>();
        if (workflowContext.getComment().equals("00001")) {
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId("zwb");
            userInfo.setUserName("庄伟斌");
            result.add(userInfo);
        }
        return result;
    }

    @Override
    public String key() {
        return "$roleIds";
    }

}
