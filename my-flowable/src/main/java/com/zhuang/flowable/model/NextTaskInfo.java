package com.zhuang.flowable.model;

import lombok.Data;

import java.util.List;

@Data
public class NextTaskInfo {

    private String taskKey;
    private String taskName;
    private Boolean isCountersign;
    private List<UserInfo> userList;

    public void setTaskName(String taskName) {
        if (taskKey.equals("_endTask_")) {
            this.taskName = "结束";
        } else {
            this.taskName = taskName;
        }
    }

}
