package com.zhuang.flowable.service;

import com.zhuang.flowable.model.UserInfo;

import java.util.List;

public interface UserService {

    UserInfo getById(String userId);

    List<UserInfo> getListByRoleId(String roleId);

}
