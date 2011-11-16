package com.baidu.rigel.service.workflow.web;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.test.TestHelper;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.test.Deployment;
import org.junit.Before;
import org.junit.Test;

import com.baidu.rigel.service.workflow.api.ContinuousPerformanceTests;

public class FormRenderProcessDeployTest {

	private ProcessEngine processEngine;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.activiti.engine.impl.test.PluggableActivitiTestCase#
	 * initializeProcessEngine()
	 */
	@Before
	public void initializeProcessEngine() {
			ClassLoader classLoader = ReflectUtil.getClassLoader();
			Enumeration<URL> resources = null;
			try {
				resources = classLoader
						.getResources("com/baidu/rigel/service/workflow/activiti.cfg.xml");
			} catch (IOException e) {
				throw new ActivitiException(
						"problem retrieving activiti.cfg.xml resources on the classpath: "
								+ System.getProperty("java.class.path"), e);
			}

			ProcessEngines.retry(resources.nextElement().toString());
			try {
				Field field = ProcessEngines.class
						.getDeclaredField("isInitialized");
				field.setAccessible(true);
				try {
					field.setBoolean(ProcessEngines.class, true);
				} catch (IllegalArgumentException ex) {
					Logger.getLogger(ContinuousPerformanceTests.class.getName())
							.log(Level.SEVERE, null, ex);
				} catch (IllegalAccessException ex) {
					Logger.getLogger(ContinuousPerformanceTests.class.getName())
							.log(Level.SEVERE, null, ex);
				}
			} catch (NoSuchFieldException ex) {
				Logger.getLogger(ContinuousPerformanceTests.class.getName())
						.log(Level.SEVERE, null, ex);
			} catch (SecurityException ex) {
				Logger.getLogger(ContinuousPerformanceTests.class.getName())
						.log(Level.SEVERE, null, ex);
			}
			
			processEngine = ProcessEngines.getDefaultProcessEngine();
		
	}
	
	@Deployment(resources = {
			"com/baidu/rigel/service/workflow/api/sp-ms.bpmn20.xml"})
	@Test
	public void testDeploy() {
		
		TestHelper.annotationDeploymentSetUp(processEngine, this.getClass(), "testDeploy");
	}
}
