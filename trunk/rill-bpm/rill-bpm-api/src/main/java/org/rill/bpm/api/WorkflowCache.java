package org.rill.bpm.api;


public interface WorkflowCache<T> {
	
	public static interface CacheTargetRetriever<T> {
		
		T getCacheTarget(String key) throws Throwable;
	}

	T getTaskRelatedInfo(String taskInstanceId, CacheTargetRetriever<T> ctr);
	
	T getProcessRelatedInfo(String processInstanceId, CacheTargetRetriever<T> ctr);
	
	String getOrSetUserInfo(String key, String value);
	
}
