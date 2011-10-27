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
package com.baidu.rigel.service.workflow.api.activiti.support;

import java.util.logging.Level;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;

import com.baidu.rigel.service.workflow.api.ProcessCreateInteceptor;
import com.baidu.rigel.service.workflow.api.ProcessCreateInterceptorAdapter;
import com.baidu.rigel.service.workflow.api.WorkflowOperations;
import com.baidu.rigel.service.workflow.api.activiti.ActivitiAccessor;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;

/**
 * Activiti engine process create interceptor adapter.
 * @author mengran
 */
public abstract class ActivitiProcessCreateInterceptorAdapter extends ProcessCreateInterceptorAdapter implements ProcessCreateInteceptor {
    
    private ActivitiAccessor activitiAccessor;

    public final void setWorkflowAccessor(WorkflowOperations workflowAccessor) {
		if (workflowAccessor instanceof SpringProxy) {
			Object targetSource;
			try {
				targetSource = ((Advised) workflowAccessor)
						.getTargetSource().getTarget();
				while (targetSource instanceof SpringProxy) {
					targetSource = ((Advised) targetSource)
							.getTargetSource().getTarget();
				}
			} catch (Exception e) {
				throw new ProcessException(e);
			}

			activitiAccessor = ((ActivitiAccessor) (targetSource));
		} else {
			activitiAccessor = ((ActivitiAccessor) workflowAccessor);
		}
	}

    protected void doPostOperation(String engineProcessInstanceId, String businessObjectId, String processStarter) {

    	// Check context parameter
        final ProcessInstance processInstance = activitiAccessor.getRuntimeService().createProcessInstanceQuery().processInstanceId(engineProcessInstanceId).singleResult();
        if (!(processInstance instanceof ProcessInstance)) {
            throw new ProcessException("processInstance must a " + ProcessInstance.class.getName() + ".");
        }

        try {
            logger.log(Level.FINE, "Execute process create interceptor#postOperation [{0}].", this);
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
