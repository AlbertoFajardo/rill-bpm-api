package org.rill.bpm.api;

public interface WorkflowCache {

	String getTaskRelatedInfo(String taskInstanceId, WorkflowOperations.TaskInformations key);
	
	String getOrSetUserInfo(String key, String value);
}
