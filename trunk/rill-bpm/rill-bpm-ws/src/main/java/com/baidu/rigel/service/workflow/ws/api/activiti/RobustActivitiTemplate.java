package com.baidu.rigel.service.workflow.ws.api.activiti;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.runtime.ProcessInstance;

import com.baidu.rigel.service.workflow.api.activiti.ActivitiTemplate;
import com.baidu.rigel.service.workflow.ws.api.RemoteWorkflowOperations.RemoteWorkflowResponse;

public class RobustActivitiTemplate extends ActivitiTemplate {

	public static final String NEXT_TASK_IDS_KEY = ActivitiTemplate.class.getName() + ".NEXT_TASK_IDS_KEY";
	
	@Override
	protected WorkflowResponse doCompleteTaskInstance(
			final String engineTaskInstanceId, final String operator, final Map<String, Object> workflowParams) {
		
		// Do super's logic first
		WorkflowResponse superResult = super.doCompleteTaskInstance(engineTaskInstanceId, operator, workflowParams);
		
		recordTaskCompleteInfo(engineTaskInstanceId, workflowParams, superResult.getEngineTaskInstanceIds());
		
		return superResult;
	}

	protected void recordTaskCompleteInfo(String engineTaskInstanceId,
			Map<String, Object> passToEngine, List<String> taskIds) {
				
		// Put it into cache.
		getCache().putTaskExecutionInfo(engineTaskInstanceId, NEXT_TASK_IDS_KEY, XStreamSerializeHelper.serializeXml(NEXT_TASK_IDS_KEY, taskIds));

	}
	
	@SuppressWarnings("unchecked")
	public RemoteWorkflowResponse handleTaskInstanceHasEnd(String engineTaskInstanceId, CommandContext commandContext) {
		
		HashMap<String, String> extendAttrs = getTaskInstanceExtendAttrs(engineTaskInstanceId);
		
		String engineProcessInstanceId = extendAttrs.get(TaskInformations.PROCESS_INSTANCE_ID.name());
		String businessKey = extendAttrs.get(TaskInformations.BUSINESS_OBJECT_ID.name());
		String rootProcessInstanceId = extendAttrs.get(TaskInformations.ROOT_PROCESS_INSTANCE_ID.name());
		// FIXME: Cache process definition key.
//		String processDefinitionKey = extendAttrs.
		String nextTaskIds = getCache().getTaskExecutionInfo(engineTaskInstanceId, RobustActivitiTemplate.NEXT_TASK_IDS_KEY);
		List<String> taskIds = new ArrayList<String>();
		if (XStreamSerializeHelper.isXStreamSerialized(nextTaskIds)) {
			taskIds = XStreamSerializeHelper.deserializeObject(nextTaskIds, RobustActivitiTemplate.NEXT_TASK_IDS_KEY, List.class);
		}
		
		ProcessInstance processInstance = getRuntimeService().createProcessInstanceQuery().processInstanceId(engineProcessInstanceId).singleResult();
		return new RemoteWorkflowResponse(
				engineProcessInstanceId, businessKey,
				null, taskIds, rootProcessInstanceId, processInstance == null).setRobustReturn(true);
	}
	
}
