package org.rill.bpm.api;

public interface WorkflowCache {

	String getTaskRelatedInfo(String taskInstanceId, String taskInformationsKey);
	
	String getOrSetUserInfo(String key, String value);
}
