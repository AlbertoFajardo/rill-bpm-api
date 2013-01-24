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
package org.rill.bpm.api;

import org.springframework.context.ApplicationEvent;

/**
 * Process instance end event.
 * @author mengran
 */
public class ProcessInstanceEndEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4064636834454294813L;

	private String triggerTaskInstanceId;
	private boolean hasParentProcess = false;
	private Object triggerTaskExecutionContext;
	
	/**
	 * @return the triggerTaskExecutionContext
	 */
	public final Object getTriggerTaskExecutionContext() {
		return triggerTaskExecutionContext;
	}

	public boolean isHasParentProcess() {
		return hasParentProcess;
	}

	/**
	 * @return the triggerTaskInstanceId
	 */
	public String getTriggerTaskInstanceId() {
		return triggerTaskInstanceId;
	}

	/**
	 * @return the processInstanceId
	 */
	public String getProcessInstanceId() {
		return (String) source;
	}

	public ProcessInstanceEndEvent(Object source) {
		super(source);
	}
	
	public ProcessInstanceEndEvent(Object source, String triggerTaskInstanceId) {
		super(source);
		
		this.triggerTaskInstanceId = triggerTaskInstanceId;
	}
	
	public ProcessInstanceEndEvent(Object source, String triggerTaskInstanceId, boolean hasParentProcess) {
		this(source, triggerTaskInstanceId);
		
		this.hasParentProcess = hasParentProcess;
	}
	
	public ProcessInstanceEndEvent(Object source, String triggerTaskInstanceId, 
			boolean hasParentProcess, Object taskExecutionContext) {
		this(source, triggerTaskInstanceId, hasParentProcess);
		
		this.triggerTaskExecutionContext = taskExecutionContext;
	} 
}
