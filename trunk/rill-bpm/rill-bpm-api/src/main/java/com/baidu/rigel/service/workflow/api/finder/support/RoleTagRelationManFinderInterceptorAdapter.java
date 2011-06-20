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
package com.baidu.rigel.service.workflow.api.finder.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ObjectUtils;

import com.baidu.rigel.service.workflow.api.ThreadLocalResourceHolder;
import com.baidu.rigel.service.workflow.api.finder.RoleTagRelationalManFinder;

public class RoleTagRelationManFinderInterceptorAdapter implements MethodInterceptor {

	private static final Log log = LogFactory.getLog(RoleTagRelationManFinderInterceptorAdapter.class);
	// Thread flag
	private static final String FINDER_CHAIN_MATCH_ALREADY = RoleTagRelationManFinderInterceptorAdapter.class.getName() + ".FINDER_CHAIN_MATCH_ALREADY";
	
	private RoleTagRelationalManFinder[] finder;


	/**
	 * @return the finder
	 */
	public RoleTagRelationalManFinder[] getFinder() {
		return finder;
	}

	/**
	 * @param finder the finder to set
	 */
	public void setFinder(RoleTagRelationalManFinder[] finder) {
		this.finder = finder;
	}
	
	
	@SuppressWarnings("unchecked")
	private boolean setMatchFinderComponent(RoleTagRelationalManFinder finder) {
		
		boolean isFirstMatch = false;
		
		List<RoleTagRelationalManFinder> executeList = null;
		if (ThreadLocalResourceHolder.getProperty(FINDER_CHAIN_MATCH_ALREADY) == null) {
			executeList = new ArrayList<RoleTagRelationalManFinder>();
			isFirstMatch = true;
			ThreadLocalResourceHolder.bindProperty(FINDER_CHAIN_MATCH_ALREADY, executeList);
		} else {
			executeList = (List<RoleTagRelationalManFinder>) ThreadLocalResourceHolder.getProperty(FINDER_CHAIN_MATCH_ALREADY);
		}
		executeList.add(finder);
		
		return isFirstMatch;
	}
	
	@SuppressWarnings("unchecked")
	public static final List<RoleTagRelationalManFinder> getMatchFinderComponent() {
		
		return (List<RoleTagRelationalManFinder>) ThreadLocalResourceHolder.getProperty(FINDER_CHAIN_MATCH_ALREADY);
	}
	
	protected final void clearFirstMatchFinderComponent() {
		
		ThreadLocalResourceHolder.unbindProperty(FINDER_CHAIN_MATCH_ALREADY);
	}

	public Object invoke(MethodInvocation arg0) throws Throwable {
		
		boolean firstMatch = false;
		try {
			
			// Check the return value is empty or not
			String[] foundMans = null;
			for (RoleTagRelationalManFinder f : this.finder) {
				
				// Set finder component
				boolean internalFirstMatch = setMatchFinderComponent(f);
				if (!firstMatch && internalFirstMatch) {
					firstMatch = true;
				}
				
				String[] fm = f.findTaskExemans((String) arg0.getArguments()[0], 
					(String) arg0.getArguments()[1], (String) arg0.getArguments()[2], (Long) arg0.getArguments()[3]);
				
				if (!ObjectUtils.isEmpty(fm)) {
					for (String s : fm) {
						foundMans = (String[]) ObjectUtils.addObjectToArray(foundMans, s);
					}
				}
			}
			
			if (!ObjectUtils.isEmpty(foundMans)) {
				if (log.isDebugEnabled())
					log.debug("Found result " + ObjectUtils.getDisplayString(foundMans) + " by " + this.finder.getClass().getName());
				
				List<String> list = Arrays.asList(foundMans);
				Set<String> afterFilter = new LinkedHashSet<String>(list);
				return afterFilter.toArray(new String[afterFilter.size()]);
			}
			
			// Let next intercepter do it.
			return arg0.proceed();
		} finally {
			if (firstMatch) {
				clearFirstMatchFinderComponent();
			}
		}
	}

}
