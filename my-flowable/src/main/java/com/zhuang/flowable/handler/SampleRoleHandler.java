package com.zhuang.flowable.handler;

import com.zhuang.flowable.listener.ProcessContext;
import com.zhuang.flowable.model.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class SampleRoleHandler implements NextTaskUserHandler {

    @Override
    public List<UserInfo> execute(ProcessContext processContext) {
        List<UserInfo> result = new ArrayList<>();
        if (processContext.getComment().equals("00001")) {
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
