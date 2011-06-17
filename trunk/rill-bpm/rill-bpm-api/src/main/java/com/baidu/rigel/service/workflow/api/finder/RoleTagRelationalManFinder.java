/**
 * 
 */
package com.baidu.rigel.service.workflow.api.finder;

import com.baidu.rigel.service.workflow.api.ThreadLocalResourceHolder;
import com.baidu.rigel.service.workflow.api.exception.TaskUnExecutableException;


/**
 * @author rillmeng
 *
 */
public interface RoleTagRelationalManFinder {

	/**
	 * 通过这个KEY值可以得到上下文中的DTO，由特定Finder组件实现相关类放入
	 * @see ThreadLocalResourceHolder#getProperty(Object)
	 */
	String FINDER_EXTRA_DTO_HOLDER_KEY = RoleTagRelationalManFinder.class.getName() + ".FINDER_EXTRA_DTO_KEY";
	
	/**
	 * 根据传入的Role Tag找到对应的人。
	 * @param roleTag 角色的标识
	 * @param engineProcessId 引擎流程ID
	 * @param engineTaskId 引擎任务ID
	 * @param processInstanceId 流程实例ID
	 * @return 找到的人。<code>NULL</code> if not found.
	 * @throws IllegalArgumentException if parameter given is empty.
	 * @throws TaskUnExecutableException if internal error occurred
	 * @see org.springframework.util.StringUtils#hasText(String)
	 */
	String[] findTaskExemans(String roleTag, String engineProcessId, String engineTaskId, Long processInstanceId)
		throws TaskUnExecutableException;
}
