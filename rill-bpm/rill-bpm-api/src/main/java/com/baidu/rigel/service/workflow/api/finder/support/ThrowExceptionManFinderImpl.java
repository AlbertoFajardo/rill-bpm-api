package com.baidu.rigel.service.workflow.api.finder.support;

import com.baidu.rigel.service.workflow.api.exception.TaskUnExecutableException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.rigel.service.workflow.api.finder.RoleTagRelationalManFinder;

public class ThrowExceptionManFinderImpl implements RoleTagRelationalManFinder {

	private static final Log log = LogFactory.getLog(ThrowExceptionManFinderImpl.class);
	
	public String[] findTaskExemans(String roleTag, String gwfpProcessId,String gwfpTaskId, Long processInstanceId) {
		
		if (log.isDebugEnabled())
			log.debug("This class can not use standlone. Please use another implementation");
		
		throw new TaskUnExecutableException("根据执行人角色[" + roleTag + "]无法找到任务执行人，请重新进行工作量分配或检查管辖关系设置。");
	}

}
