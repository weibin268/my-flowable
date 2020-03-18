package com.zhuang.flowable.service;

import com.zhuang.flowable.model.UserInfo;
import com.zhuang.flowable.service.UserService;

import java.util.ArrayList;
import java.util.List;

public class SampleUserService implements UserService {

    public UserInfo getUser(String userId) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        if (userId == null) {
            userInfo.setUserName("");

        } else if (userId.equals("zwb")) {
            userInfo.setUserName("庄伟斌");

        } else if (userId.equals("admin")) {
            userInfo.setUserName("管理员");
        } else if (userId.equals("zs")) {
            userInfo.setUserName("张三");
        } else if (userId.equals("ls")) {
            userInfo.setUserName("李四");
        } else if (userId.equals("wb")) {
            userInfo.setUserName("王五");
        } else if (userId.equals("zl")) {
            userInfo.setUserName("赵六");
        }
        return userInfo;

    }

    public List<UserInfo> getUsersByRoleId(String roleId) {
        List<UserInfo> userInfos = new ArrayList<UserInfo>();
        if (roleId.equals("sys")) {
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId("admin");
            userInfo.setUserName("管理员");
            userInfos.add(userInfo);

            userInfo = new UserInfo();
            userInfo.setUserId("zwb");
            userInfo.setUserName("庄伟斌");
            userInfos.add(userInfo);

        } else if (roleId.equals("mgr")) {
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId("zs");
            userInfo.setUserName("张三");
            userInfos.add(userInfo);

        } else if (roleId.equals("def")) {
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId("ls");
            userInfo.setUserName("李四");
            userInfos.add(userInfo);

            userInfo = new UserInfo();
            userInfo.setUserId("wb");
            userInfo.setUserName("王五");
            userInfos.add(userInfo);

            userInfo = new UserInfo();
            userInfo.setUserId("zl");
            userInfo.setUserName("赵六");
            userInfos.add(userInfo);
        }
        return userInfos;
    }

    public List<UserInfo> getUsersByRoleName(String roleName) {
        List<UserInfo> userInfos = new ArrayList<UserInfo>();
        return userInfos;
    }

}
