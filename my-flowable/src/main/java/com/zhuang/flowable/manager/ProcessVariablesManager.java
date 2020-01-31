package com.zhuang.flowable.manager;

import org.flowable.engine.HistoryService;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessVariablesManager {
	
	@Autowired
	private HistoryService historyService;
	
	public Map<String, Object> getProcessVariablesByTaskId(String taskId)
	{
		Map<String, Object> result=new HashMap<String, Object>();
		
		HistoricTaskInstance historicTaskInstance= historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
		
	    List<HistoricVariableInstance> historicVariableInstances =historyService.createHistoricVariableInstanceQuery().processInstanceId(historicTaskInstance.getProcessInstanceId()).list();
		
	    for (HistoricVariableInstance historicVariableInstance : historicVariableInstances) {
			result.put(historicVariableInstance.getVariableName(), historicVariableInstance.getValue());
		}
	    
		return result;
	}
}
