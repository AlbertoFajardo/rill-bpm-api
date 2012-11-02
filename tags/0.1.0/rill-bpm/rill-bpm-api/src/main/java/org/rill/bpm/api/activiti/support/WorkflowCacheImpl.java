package org.rill.bpm.api.activiti.support;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.api.WorkflowCache;
import org.rill.bpm.api.exception.ProcessException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.Assert;


public class WorkflowCacheImpl implements WorkflowCache<HashMap<String, String>> {

	protected final Log logger = LogFactory.getLog(getClass().getName());
	
	@Override
	@Cacheable(value = { "PERSISTENT_CACHE" }, key="#key")
	public String getOrSetUserInfo(String key, String value) {
		
		Assert.notNull(key, "Unsupported null key");
		Assert.notNull(value, "Unsupported null value of key " + key);
		
		logger.debug("NOT HIT: method[getOrSetUserInfo] cache key:" + key + ", value:" + value);
		return value;
	}
	
	@Override
	@Cacheable(value = { "PERSISTENT_CACHE" }, key="#key")
	public String getOrSetUserInfo(
			String key,
			org.rill.bpm.api.WorkflowCache.CacheTargetRetriever<String> valueRetriever) {
		
		Assert.notNull(key, "Unsupported null key");
		Assert.notNull(valueRetriever, "Unsupported null valueRetriever of key " + key);
		
		logger.debug("NOT HIT: method[getOrSetUserInfo] cache key:" + key);
		try {
			return valueRetriever.getCacheTarget(key);
		} catch (Throwable e) {
			logger.error("Can not retrieve user info cache." + key, e);
			throw new ProcessException(e);
		}
		
	}
	
	@Override
	@Cacheable(value = { "taskCache" }, key = "#taskInstanceId")
	public HashMap<String, String> getTaskRelatedInfo(String taskInstanceId, CacheTargetRetriever<HashMap<String, String>> ctr) {
		
		Assert.notNull(taskInstanceId, "Unsupported null key");
		
		logger.debug("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId);
		try {
			return ctr.getCacheTarget(taskInstanceId);
		} catch (Throwable e) {
			logger.error("Can not retrieve task info cache." + taskInstanceId, e);
			throw new ProcessException(e);
		}
	}

	@Override
	@Cacheable(value = { "processCache" }, key = "#processInstanceId")
	public HashMap<String, String> getProcessRelatedInfo(String processInstanceId, CacheTargetRetriever<HashMap<String, String>> ctr) {
		
		Assert.notNull(processInstanceId, "Unsupported null key");
		
		logger.debug("NOT HIT: method[getProcessRelatedInfo] cache key:" + processInstanceId);
		try {
			return ctr.getCacheTarget(processInstanceId);
		} catch (Throwable e) {
			logger.error("Can not retrieve process info cache." + processInstanceId, e);
			throw new ProcessException(e);
		}
	}

}
