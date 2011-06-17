package com.baidu.rigel.service.workflow.api.finder.support;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.support.AbstractRegexpMethodPointcut;
import org.springframework.aop.support.DynamicMethodMatcherPointcut;
import org.springframework.aop.support.JdkRegexpMethodPointcut;
import org.springframework.util.StringUtils;

import com.baidu.rigel.service.workflow.api.ThreadLocalResourceHolder;

public class RegexpMethodAndParamValuePointCut extends
		DynamicMethodMatcherPointcut {

	private static final Log log = LogFactory.getLog(RegexpMethodAndParamValuePointCut.class);
	
	private static final String METHOD_ARGUMENT_KEY = RegexpMethodAndParamValuePointCut.class.getName() + ".METHOD_ARGUMENT_KEY";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private AbstractRegexpMethodPointcut staticRegexpMethodPointcut;

	public void setPatterns(String[] patterns) {
		
		// Context inject
		staticRegexpMethodPointcut = new InternalRegexpMethodPointcut();
		staticRegexpMethodPointcut.setPatterns(patterns);
	}


	private final class InternalRegexpMethodPointcut extends JdkRegexpMethodPointcut {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unchecked")
		public boolean matches(Method method, Class targetClass) {
			
			String roleTag = (String) ThreadLocalResourceHolder.getProperty(METHOD_ARGUMENT_KEY);
			
			if (log.isDebugEnabled())
				log.debug("Try to match pattern[" + targetClass.getName() + "." + method.getName() + roleTag + "]," +
						"[" + method.getDeclaringClass().getName() + "." + method.getName() + roleTag + "]");
			
			return ((targetClass != null && matchesPattern(targetClass.getName() + "." + method.getName() + roleTag)) ||
					matchesPattern(method.getDeclaringClass().getName() + "." + method.getName() + roleTag));
		}
		
	} 
	
	/**
	 * 取得此PointCut的pattern signature
	 * @param args 参数列表
	 * @return pattern signature
	 */
	protected String obtainPattern(Object[] args) {
		
		// Get parameter value
		// At here it must a String value
		// See com.baidu.rigel.sp.workflow.finder.RoleTagRelationalManFinder#foundMansByRoleTag(String)
		if (args == null || args.length == 0 || args[0] == null || 
				!(args[0] instanceof String) || !StringUtils.hasText((String) args[0]))
			throw new IllegalArgumentException("Argument is not match. " +
					"See com.baidu.rigel.sp.workflow.finder.RoleTagRelationalManFinder#foundMansByRoleTag(String)");
		
		String roleTag = (String) args[0];
		
		return roleTag;
	}

	@SuppressWarnings("unchecked")
	public boolean matches(Method method, Class targetClass, Object[] args) {
		
		// Assemble pattern signature
		String roleTag = "(" + obtainPattern(args) + ")";
		
		// Put it on thread local resource holder
		ThreadLocalResourceHolder.bindProperty(METHOD_ARGUMENT_KEY, roleTag);
		
		// Delegate static point cut
		boolean matchResult = staticRegexpMethodPointcut.matches(method, targetClass);
		
		// Remove it from thread local resource holder
		ThreadLocalResourceHolder.unbindProperty(METHOD_ARGUMENT_KEY);
		
		return matchResult;
	}
	
	

}
