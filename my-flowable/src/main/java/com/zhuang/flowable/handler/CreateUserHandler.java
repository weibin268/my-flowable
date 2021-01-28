package com.zhuang.flowable.handler;

import com.zhuang.flowable.listener.ProcessContext;
import com.zhuang.flowable.manager.ProcessInstanceManager;
import com.zhuang.flowable.model.UserInfo;
import com.zhuang.flowable.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CreateUserHandler implements NextTaskUserHandler {

    @Autowired
    private ProcessInstanceManager processInstanceManager;
    @Autowired
    private UserService userService;

    @Override
    public List<UserInfo> execute(ProcessContext processContext) {
        List<UserInfo> result = new ArrayList<>();
        String startUserId = processInstanceManager.getStartUserIdByTaskId(processContext.getTaskId());
        UserInfo user = userService.getById(startUserId);
        result.add(user);
        return result;
    }

    @Override
    public String key() {
        return "$createUser";
    }

}
