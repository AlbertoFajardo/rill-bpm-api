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
