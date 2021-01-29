package com.zhuang.flowable.impl;

import com.zhuang.flowable.MyFlowableTestApplicationTest;
import com.zhuang.flowable.model.FlowInfo;
import com.zhuang.flowable.model.PageInfo;
import com.zhuang.flowable.model.ProcDef;
import com.zhuang.flowable.model.TaskInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlowableWorkflowQueryTest extends MyFlowableTestApplicationTest {

    @Autowired
    FlowableWorkflowQuery flowableWorkflowQuery;

    @Test
    void getMyTodoListPage() {
        PageInfo<FlowInfo> flowInfoPageInfo= flowableWorkflowQuery.getMyTodoListPage("zs", 1, 100, new HashMap<>());
        System.out.println(flowInfoPageInfo);
        flowInfoPageInfo.getList().stream().forEach(System.out::println);
    }

    @Test
    void getMyDoneListPage() {
        PageInfo<FlowInfo> flowInfoPageInfo = flowableWorkflowQuery.getMyDoneListPage("zs", 1, 100, new HashMap<>());
        System.out.println(flowInfoPageInfo);
        flowInfoPageInfo.getList().stream().forEach(System.out::println);
    }

    @Test
    void getHistoryTaskInfoList(){
        List<TaskInfo> taskInfoList = flowableWorkflowQuery.getHistoryTaskInfoList("32cd56e9-6247-11eb-8037-005056c00001");
        taskInfoList.stream().forEach(System.out::println);
    }

    @Test
    void getProcDefList() {
        List<ProcDef> procDefList = flowableWorkflowQuery.getProcDefList();
        procDefList.stream().forEach(System.out::println);
    }
}