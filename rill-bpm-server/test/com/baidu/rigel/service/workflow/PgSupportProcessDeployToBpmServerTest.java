package com.baidu.rigel.service.workflow;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.test.TestHelper;
import org.activiti.engine.test.Deployment;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <b>Must start h2.DB server first.<b/>
 * @author mengran
 */
public class PgSupportProcessDeployToBpmServerTest {

    public PgSupportProcessDeployToBpmServerTest() {
    }
    protected static ProcessEngine cachedProcessEngine;

    protected static void initializeProcessEngine() {
        if (cachedProcessEngine == null) {
            cachedProcessEngine = ProcessEngines.getDefaultProcessEngine();
            if (cachedProcessEngine == null) {
                throw new ActivitiException("no default process engine available");
            }
        }
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

        initializeProcessEngine();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    // The methods must be annotated with annotation @Test. For example:
    //

    @Deployment(resources = {"com/baidu/rigel/service/workflow/api/pg-support.bpmn20.xml"})
    public void panguDeploy() {
        // Do nothing
    }
    
    @Deployment(resources = {"com/baidu/rigel/service/workflow/api/pg-support-simplevariable.bpmn20.xml"})
    public void panguSimpleVarDeploy() {
    	
    }

    @Test
    public void deployPgSupportProcessToBpmServer() {

        TestHelper.annotationDeploymentSetUp(cachedProcessEngine, getClass(), "panguDeploy");
        
        TestHelper.annotationDeploymentSetUp(cachedProcessEngine, getClass(), "panguSimpleVarDeploy");
    }
}
