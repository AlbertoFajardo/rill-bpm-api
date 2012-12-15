package org.rill.bpm;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.test.TestHelper;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;


/**
 * Use default engine.
 * @author mengran
 *
 */
public class PeerMethodTestHelperTaskExecutionListener extends
		AbstractTestExecutionListener {

	private String deploymentSetUp;
    
	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		super.beforeTestMethod(testContext);
		
    	deploymentSetUp = TestHelper.annotationDeploymentSetUp(ProcessEngines.getDefaultProcessEngine(), testContext.getTestClass(), testContext.getTestMethod().getName());
    	
	}

	@Override
	public void afterTestMethod(TestContext testContext) throws Exception {
		super.afterTestMethod(testContext);
		
    	TestHelper.annotationDeploymentTearDown(ProcessEngines.getDefaultProcessEngine(), deploymentSetUp, testContext.getTestClass(), testContext.getTestMethod().getName());
	}

	
}
