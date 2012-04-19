package org.rill.bpm;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.test.TestHelper;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;


public class PeerClassTestHelperTaskExecutionListener extends
		AbstractTestExecutionListener {

	private String deploymentSetUp;
    
	@Override
	public void beforeTestClass(TestContext testContext) throws Exception {
		super.beforeTestClass(testContext);
		
    	deploymentSetUp = TestHelper.annotationDeploymentSetUp(ProcessEngines.getDefaultProcessEngine(), testContext.getTestClass(), "deploy");
    	
	}

	@Override
	public void afterTestClass(TestContext testContext) throws Exception {
		super.afterTestClass(testContext);
		
    	TestHelper.annotationDeploymentTearDown(ProcessEngines.getDefaultProcessEngine(), deploymentSetUp, testContext.getTestClass(), "deploy");
	}
	
}
