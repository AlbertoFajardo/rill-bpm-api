package org.rill.bpm.api.scaleout;

import org.rill.bpm.api.TaskExecutionContext;
import org.rill.bpm.api.TaskLifecycleInteceptorAdapter;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.activiti.ActivitiAccessor;

public class ScaleoutTaskLifecycleInterceptor extends TaskLifecycleInteceptorAdapter {
		
	@Override
	protected void doInit(TaskExecutionContext taskExecutionContext) {
		
		WorkflowOperations impl = ScaleoutHelper.determineImplWithBusinessKey(getWorkflowCache(), getWorkflowAccessor(), taskExecutionContext.getBusinessObjectId());
		ActivitiAccessor activitiAccessor = ActivitiAccessor.retrieveActivitiAccessorImpl(impl, ActivitiAccessor.class);
		
		// For scale-out mechanism
        activitiAccessor.getProcessInstanceInformations(taskExecutionContext.getProcessInstanceId());
        
	}
	
}
