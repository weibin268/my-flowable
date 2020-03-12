package com.zhuang.flowable.impl;

import com.zhuang.flowable.MyFlowableTestApplicationTest;
import com.zhuang.flowable.model.FlowInfo;
import com.zhuang.flowable.model.PageInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class FlowableWorkflowQueryTest extends MyFlowableTestApplicationTest {

    @Autowired
    FlowableWorkflowQuery flowableWorkflowQuery;

    @Test
    void getMyTodoListPage() {

        PageInfo<FlowInfo> flowInfoPageInfo= flowableWorkflowQuery.getMyTodoListPage("1", 1, 100, new HashMap<>());
        System.out.println(flowInfoPageInfo);
    }
}