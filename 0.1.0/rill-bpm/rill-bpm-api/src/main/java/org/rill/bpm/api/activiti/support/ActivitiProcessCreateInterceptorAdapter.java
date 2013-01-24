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
package org.rill.bpm.api.activiti.support;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.rill.bpm.api.ProcessCreateInteceptor;
import org.rill.bpm.api.ProcessCreateInterceptorAdapter;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.activiti.ActivitiAccessor;
import org.rill.bpm.api.exception.ProcessException;
import org.rill.bpm.api.scaleout.ScaleoutHelper;


/**
 * Activiti engine process create interceptor adapter.
 * @author mengran
 */
public abstract class ActivitiProcessCreateInterceptorAdapter extends ProcessCreateInterceptorAdapter implements ProcessCreateInteceptor {

	protected final void doPostOperation(String processDefinitionKey, String engineProcessInstanceId, String businessObjectId, String processStarter) {

		WorkflowOperations impl = ScaleoutHelper.determineImpl(getWorkflowCache(), getWorkflowAccessor(), ScaleoutHelper.generateScaloutKey(businessObjectId));
		ActivitiAccessor activitiAccessor = ActivitiAccessor.retrieveActivitiAccessorImpl(impl, ActivitiAccessor.class);
    	// Check context parameter
        final ProcessInstance processInstance = activitiAccessor.getRuntimeService().createProcessInstanceQuery().processInstanceId(engineProcessInstanceId).singleResult();
        if (!(processInstance instanceof ProcessInstance)) {
            throw new ProcessException("processInstance must a " + ProcessInstance.class.getName() + ".");
        }

        try {
            logger.debug("Execute process create interceptor#postOperation " + this.getClass().getName());
            activitiAccessor.runExtraCommand(new Command<Void>() {

				@Override
				public Void execute(CommandContext commandContext) {
					
					// Initialize process definition if need.
					if (processInstance instanceof ExecutionEntity) {
						((ExecutionEntity) processInstance).getProcessDefinition();
					}
					return null;
				}
            	
            });
            
            // FIXME: Need proxy it for prevent call some method.
            doPostOperation(processInstance, businessObjectId, processStarter);
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }
    
    protected void doPostOperation(ProcessInstance engineProcessInstance, String businessObjectId, String processStarter) {

        // Do nothing
    }
}
