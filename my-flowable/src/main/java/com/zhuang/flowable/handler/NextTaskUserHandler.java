package com.zhuang.flowable.handler;

import com.zhuang.flowable.listener.ProcessContext;
import com.zhuang.flowable.model.UserInfo;

import java.util.List;

public interface NextTaskUserHandler {

    List<UserInfo> execute(ProcessContext processContext);

    String key();
}
