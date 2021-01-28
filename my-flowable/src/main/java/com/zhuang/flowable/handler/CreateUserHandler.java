package com.zhuang.flowable.handler;

import com.zhuang.flowable.WorkflowContext;
import com.zhuang.flowable.model.UserInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CreateUserHandler implements NextTaskUserHandler {

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
        return "$createUser";
    }

}
