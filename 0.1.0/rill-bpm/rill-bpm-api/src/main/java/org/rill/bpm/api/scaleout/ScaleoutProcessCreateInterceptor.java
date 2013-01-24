package org.rill.bpm.api.scaleout;

import org.activiti.engine.runtime.ProcessInstance;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.activiti.ActivitiAccessor;
import org.rill.bpm.api.activiti.support.ActivitiProcessCreateInterceptorAdapter;

public class ScaleoutProcessCreateInterceptor extends
		ActivitiProcessCreateInterceptorAdapter {

	@Override
	protected void doPostOperation(ProcessInstance engineProcessInstance,
			String businessObjectId, String processStarter) {
		
		WorkflowOperations impl = ScaleoutHelper.determineImplWithBusinessKey(getWorkflowCache(), getWorkflowAccessor(), businessObjectId);
		ActivitiAccessor activitiAccessor = ActivitiAccessor.retrieveActivitiAccessorImpl(impl, ActivitiAccessor.class);
		
		// For scale-out mechanism
        activitiAccessor.getProcessInstanceInformations(engineProcessInstance.getProcessInstanceId());
        
	}
	
}
