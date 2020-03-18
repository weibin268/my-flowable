package com.zhuang.flowable.service;

import com.zhuang.flowable.model.UserInfo;

import java.util.List;

public interface UserService {
	
	UserInfo getUser(String userId);
	
	List<UserInfo> getUsersByRoleId(String roleId);
	
	List<UserInfo> getUsersByRoleName(String roleName);
	
}
