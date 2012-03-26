package org.rill.bpm.web;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.api.WorkflowCache;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.scaleout.ScaleoutHelper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.util.CookieGenerator;

public class ScaleoutControllerSupport implements InitializingBean {

	protected final Log LOGGER = LogFactory.getLog(ScaleoutControllerSupport.class);
	
	public static final String SCALE_OUT_TARGET = "selectedScaleoutTarget";
	
	@Resource
	private WorkflowOperations workflowAccessor;

	public final WorkflowOperations getWorkflowAccessor() {
		return workflowAccessor;
	}

	public final void setWorkflowAccessor(WorkflowOperations workflowAccessor) {
		this.workflowAccessor = workflowAccessor;
	}
	
	@Resource(name="workflowCache")
	private WorkflowCache<HashMap<String, String>> workflowCache;
	
	public final WorkflowCache<HashMap<String, String>> getWorkflowCache() {
		return workflowCache;
	}

	public final void setWorkflowCache(WorkflowCache<HashMap<String, String>> workflowCache) {
		this.workflowCache = workflowCache;
	}
	
	protected volatile ConcurrentHashMap<String, WorkflowOperations> scaleoutTarget = new ConcurrentHashMap<String, WorkflowOperations>();
	protected final CookieGenerator scaleoutTargetCookie = new CookieGenerator();
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		scaleoutTarget = ScaleoutHelper.getScaleoutTarget(getWorkflowAccessor());
		
		// Initialize
		scaleoutTargetCookie.setCookieName(SCALE_OUT_TARGET);
		scaleoutTargetCookie.setCookieMaxAge(Integer.MAX_VALUE);
	}
	
}
