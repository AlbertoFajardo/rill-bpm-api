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
package com.baidu.rigel.service.workflow.api.finder;

import com.baidu.rigel.service.workflow.api.ThreadLocalResourceHolder;
import com.baidu.rigel.service.workflow.api.exception.TaskUnExecutableException;


/**
 * @author rillmeng
 *
 */
public interface RoleTagRelationalManFinder {

	/**
	 * Obtain thread-binding DTO.
	 * @see ThreadLocalResourceHolder#getProperty(Object)
	 */
	String FINDER_EXTRA_DTO_HOLDER_KEY = RoleTagRelationalManFinder.class.getName() + ".FINDER_EXTRA_DTO_KEY";
	
	/**
	 * Task executor finder.
	 * @param roleTag role tag
	 * @param engineProcessId engine process instance ID
	 * @param engineTaskId engine task ID
	 * @param processInstanceId local process engine ID
	 * @return Founded executor <code>NULL</code> if not found.
	 * @throws IllegalArgumentException if parameter given is empty.
	 * @throws TaskUnExecutableException if internal error occurred
	 * @see org.springframework.util.StringUtils#hasText(String)
	 */
	String[] findTaskExemans(String roleTag, String engineProcessId, String engineTaskId, Long processInstanceId)
		throws TaskUnExecutableException;
}
