package com.zhuang.flowable.handler;

import com.zhuang.flowable.listener.ProcessContext;
import com.zhuang.flowable.model.UserInfo;
import com.zhuang.flowable.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoleIdsHandler implements NextTaskUserHandler {

    @Autowired
    private UserService userService;

    @Override
    public List<UserInfo> execute(ProcessContext processContext) {
        String roleIds = processContext.getComment();
        List<String> roleIdList = Arrays.asList(roleIds.split(","));
        return roleIdList.stream().flatMap(item -> userService.getListByRoleId(item).stream()).collect(Collectors.toList());
    }

    @Override
    public String key() {
        return "$roleIds";
    }

}
