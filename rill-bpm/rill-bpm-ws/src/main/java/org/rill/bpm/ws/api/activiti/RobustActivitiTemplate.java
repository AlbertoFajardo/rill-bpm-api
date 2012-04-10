package org.rill.bpm.ws.api.activiti;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.runtime.ProcessInstance;
import org.rill.bpm.api.WorkflowCache.CacheTargetRetriever;
import org.rill.bpm.api.activiti.ActivitiTemplate;
import org.rill.bpm.api.exception.ProcessException;
import org.rill.bpm.ws.api.RemoteWorkflowOperations.RemoteWorkflowResponse;
import org.springframework.util.ObjectUtils;


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

	protected String generateNextTaskInsKey(String engineTaskInstanceId) {
		
		return NEXT_TASK_IDS_KEY + "." + engineTaskInstanceId;
	}
	
	protected void recordTaskCompleteInfo(String engineTaskInstanceId,
			Map<String, Object> passToEngine, List<String> taskIds) {
				
		// Put it into cache.
		String fromCache = getWorkflowCache().getOrSetUserInfo(generateNextTaskInsKey(engineTaskInstanceId), XStreamSerializeHelper.serializeXml(NEXT_TASK_IDS_KEY, taskIds));
		logger.info("Put next task ids " + ObjectUtils.getDisplayString(taskIds) + " into cache, return from cache is " + fromCache);
	}
	
	@SuppressWarnings("unchecked")
	public RemoteWorkflowResponse handleTaskInstanceHasEnd(final String engineTaskInstanceId, CommandContext commandContext) {
		
		HashMap<String, String> extendAttrs = getTaskInstanceInformations(engineTaskInstanceId);
		
		String engineProcessInstanceId = extendAttrs.get(TaskInformations.PROCESS_INSTANCE_ID.name());
		String businessKey = extendAttrs.get(TaskInformations.BUSINESS_OBJECT_ID.name());
		String rootProcessInstanceId = extendAttrs.get(TaskInformations.ROOT_PROCESS_INSTANCE_ID.name());
		String processDefinitionKey = extendAttrs.get(TaskInformations.PROCESS_DEFINE_KEY.name());
		
		String nextTaskIds = getWorkflowCache().getOrSetUserInfo(generateNextTaskInsKey(engineTaskInstanceId), new CacheTargetRetriever<String>() {

			@Override
			public String getCacheTarget(String key) throws Throwable {
				logger.warn("Why lose next task ids of " + engineTaskInstanceId);
				return null;
			}
			
		});
		
		if ("".equals(nextTaskIds)) {
			throw new ProcessException("Can not find next task IDs cache by key " + generateNextTaskInsKey(engineTaskInstanceId) + ", maybe wrong retrieve timing or cache expired.");
		}
		
		List<String> taskIds = new ArrayList<String>();
		if (XStreamSerializeHelper.isXStreamSerialized(nextTaskIds)) {
			taskIds = XStreamSerializeHelper.deserializeObject(nextTaskIds, RobustActivitiTemplate.NEXT_TASK_IDS_KEY, List.class);
		}
		
		ProcessInstance processInstance = getRuntimeService().createProcessInstanceQuery().processInstanceId(engineProcessInstanceId).singleResult();
		return new RemoteWorkflowResponse(
				engineProcessInstanceId, businessKey,
				processDefinitionKey, taskIds, rootProcessInstanceId, processInstance == null).setRobustReturn(true);
	}
	
}
