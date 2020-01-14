package com.zhuang.flowable.service;

import com.zhuang.flowable.model.UserInfo;

import java.util.List;

public interface UserManagementService {
	
	UserInfo getUser(String userId);
	
	List<UserInfo> getUsersByRoleId(String roleId);
	
	List<UserInfo> getUsersByRoleName(String roleName);
	
}
