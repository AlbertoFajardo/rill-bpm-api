/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rill.bpm.api.support;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rill.bpm.api.TaskExecutionContext;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.processvar.DummyReceiptInfo;


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

    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}

    @Test
    public void engineDrivenGeneric() {

        ReceiptInfoEngineDrivenTLIAdapter adapter = new ReceiptInfoEngineDrivenTLIAdapter();
        TaskExecutionContext taskContext = new TaskExecutionContext();
        taskContext.getOtherInfos().put(WorkflowOperations.ENGINE_DRIVEN_TASK_FORM_DATA_KEY, new DummyReceiptInfo());
        adapter.preComplete(taskContext);

        Assert.assertNotNull(taskContext.getOtherInfos().get(WorkflowOperations.ENGINE_DRIVEN_TASK_RETURN_DATA_KEY));
    }
}