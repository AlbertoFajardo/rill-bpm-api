/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rill.bpm.api.finder.support;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.api.ThreadLocalResourceHolder;
import org.springframework.aop.support.AbstractRegexpMethodPointcut;
import org.springframework.aop.support.DynamicMethodMatcherPointcut;
import org.springframework.aop.support.JdkRegexpMethodPointcut;
import org.springframework.util.StringUtils;


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
		if (args == null || args.length == 0 || args[0] == null || 
				!(args[0] instanceof String) || !StringUtils.hasText((String) args[0]))
			throw new IllegalArgumentException("Argument is not match. " +
					"See RoleTagRelationalManFinder#foundMansByRoleTag(String)");
		
		String roleTag = (String) args[0];
		
		return roleTag;
	}

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
