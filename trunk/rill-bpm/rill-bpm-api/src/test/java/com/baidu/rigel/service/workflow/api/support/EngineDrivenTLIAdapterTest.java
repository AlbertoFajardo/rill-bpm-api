/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.baidu.rigel.service.workflow.api.support;

import com.baidu.rigel.service.workflow.api.WorkflowOperations;
import com.baidu.rigel.service.workflow.api.activiti.ActivitiTaskExecutionContext;
import com.baidu.rigel.service.workflow.api.processvar.DummyReceiptInfo;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author mengran
 */
public class EngineDrivenTLIAdapterTest {

    public EngineDrivenTLIAdapterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}

    @Test
    public void engineDrivenGeneric() {

        ReceiptInfoEngineDrivenTLIAdapter adapter = new ReceiptInfoEngineDrivenTLIAdapter();
        ActivitiTaskExecutionContext taskContext = new ActivitiTaskExecutionContext();
        taskContext.getWorkflowParams().put(WorkflowOperations.ENGINE_DRIVEN_TASK_FORM_DATA_KEY, new DummyReceiptInfo());
        adapter.preComplete(taskContext);

        Assert.assertNotNull(taskContext.getWorkflowParams().get(WorkflowOperations.ENGINE_DRIVEN_TASK_RETURN_DATA_KEY));
    }
}