package org.rill.bpm;

import org.activiti.engine.impl.test.TestHelper;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.activiti.ActivitiAccessor;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;


public class PeerClassTestHelperTaskExecutionListener extends
		AbstractTestExecutionListener {

	private WorkflowOperations workflowAccessor;
	private String deploymentSetUp;
    
	@Override
	public void beforeTestClass(TestContext testContext) throws Exception {
		super.beforeTestClass(testContext);
		
		workflowAccessor = testContext.getApplicationContext().getBean("workflowAccessor", WorkflowOperations.class);
		ActivitiAccessor activitiAccessor = ActivitiAccessor.retrieveActivitiAccessorImpl(workflowAccessor, ActivitiAccessor.class);
    	deploymentSetUp = TestHelper.annotationDeploymentSetUp(activitiAccessor.getProcessEngine(), testContext.getTestClass(), "deploy");
    	
	}

	@Override
	public void afterTestClass(TestContext testContext) throws Exception {
		super.afterTestClass(testContext);
		
		ActivitiAccessor activitiAccessor = ActivitiAccessor.retrieveActivitiAccessorImpl(workflowAccessor, ActivitiAccessor.class);
    	TestHelper.annotationDeploymentTearDown(activitiAccessor.getProcessEngine(), deploymentSetUp, testContext.getTestClass(), "deploy");
	}
	
}
