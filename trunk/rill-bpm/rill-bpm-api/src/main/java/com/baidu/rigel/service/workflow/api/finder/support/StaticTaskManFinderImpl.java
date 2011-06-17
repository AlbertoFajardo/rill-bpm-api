package com.baidu.rigel.service.workflow.api.finder.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ObjectUtils;

import com.baidu.rigel.service.workflow.api.finder.RoleTagRelationalManFinder;

public class StaticTaskManFinderImpl implements RoleTagRelationalManFinder {

	private static final Log log = LogFactory.getLog(StaticTaskManFinderImpl.class);
	
	private String[] exeMans;
	
	/**
	 * @return the exeMans
	 */
	public String[] getExeMans() {
		return exeMans;
	}

	/**
	 * @param exeMans the exeMans to set
	 */
	public void setExeMans(String[] exeMans) {
		this.exeMans = exeMans;
	}


	public String[] findTaskExemans(String roleTag, String gwfpProcessId,String gwfpTaskId, Long processInstanceId) {
		
		if (log.isDebugEnabled())
			log.debug("This is out-of-box class, return rejected exeMans" + ObjectUtils.getDisplayString(exeMans));
		
		return this.exeMans;
	}

}
