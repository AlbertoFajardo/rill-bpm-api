package com.baidu.rigel.service.workflow.web;

import java.util.logging.Level;

import junit.framework.AssertionFailedError;

import org.activiti.engine.impl.test.TestHelper;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.test.Deployment;

import com.baidu.rigel.service.workflow.api.PgSupportV2Test;

public class ServerPgsupportV2Test extends PgSupportV2Test {

	@Override
	public void runBare() throws Throwable {
		initializeProcessEngine();
		if (repositoryService == null) {
			initializeServices();
		}

		log.severe(EMPTY_LINE);

		try {

			deploymentId = TestHelper.annotationDeploymentSetUp(processEngine,
					getClass(), getName());

			doRunBare();

		} catch (AssertionFailedError e) {
			log.severe(EMPTY_LINE);
			log.log(Level.SEVERE, "ASSERTION FAILED: " + e, e);
			exception = e;
			throw e;

		} catch (Throwable e) {
			log.severe(EMPTY_LINE);
			log.log(Level.SEVERE, "EXCEPTION: " + e, e);
			exception = e;
			throw e;

		} finally {
			// TestHelper.annotationDeploymentTearDown(processEngine,
			// deploymentId, getClass(), getName());
			// assertAndEnsureCleanDb();
			ClockUtil.reset();
		}
	}

	/**
	 * Runs the bare test sequence.
	 * 
	 * @throws Throwable
	 *             if any exception is thrown
	 */
	public void doRunBare() throws Throwable {
		Throwable exceptionTest = null;
		setUp();
		try {
			runTest();
		} catch (Throwable running) {
			exceptionTest = running;
		} finally {
			try {
				tearDown();
			} catch (Throwable tearingDown) {
				if (exceptionTest == null) {
					exceptionTest = tearingDown;
				}
			}
		}
		if (exceptionTest != null) {
			throw exceptionTest;
		}
	}

	@Override
	@Deployment(resources = {
			"com/baidu/rigel/service/workflow/api/pg-support_v2.bpmn20.xml",
			"com/baidu/rigel/service/workflow/api/Pg-support-managerAudit_v2.bpmn20.xml",
			"com/baidu/rigel/service/workflow/api/Pg-support-orderValidityAudit_v2.bpmn20.xml",
			"com/baidu/rigel/service/workflow/api/Pg-support-resetOrderReceiptType_v2.bpmn20.xml" })
	public void testPgSupportV2() {
		
		super.testPgSupportV2();
	}

}
