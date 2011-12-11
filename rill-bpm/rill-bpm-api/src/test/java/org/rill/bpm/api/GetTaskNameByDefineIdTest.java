/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rill.bpm.api;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.rill.bpm.PeerMethodTestHelperTaskExecutionListener;
import org.rill.bpm.api.ThreadLocalResourceHolder;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.activiti.ActivitiAccessor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;


/**
 *
 * @author mengran
 */
@ContextConfiguration(value="classpath:org/rill/bpm/api/activiti.cfg.xml")
@TestExecutionListeners({PeerMethodTestHelperTaskExecutionListener.class})
public class GetTaskNameByDefineIdTest extends AbstractJUnit4SpringContextTests {

    private final Logger log = Logger.getLogger(getClass().getName());
    
    @Resource
    private WorkflowOperations workflowAccessor;
    
    @Deployment(resources = {"org/rill/bpm/api/pg-support.bpmn20.xml"})
    @Test
    public void testGetTaskNameByDefineId() throws InterruptedException {

    	ActivitiAccessor activitiAccessor = ActivitiAccessor.retrieveActivitiAccessorImpl(workflowAccessor, ActivitiAccessor.class);
        RuntimeService runtimeService = activitiAccessor.getRuntimeService();
        TaskService taskService = activitiAccessor.getTaskService();
        
        Integer orderId = new Random().nextInt();
        String processDefinitionKey = "pg-support";
        log.log(Level.FINE, "Start process by key{0}], and business key[{1}]", new Object[]{processDefinitionKey, orderId});

        try {
            log.entering("PgSupportTest", "createProcessInstance", ThreadLocalResourceHolder.printAll());
            // Start process by KEY
            workflowAccessor.createProcessInstance(processDefinitionKey, "Rill Meng", orderId.toString(), null);
        } finally {
            log.exiting("PgSupportTest", "createProcessInstance", ThreadLocalResourceHolder.printAll());
        }

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(orderId.toString(), processDefinitionKey).singleResult();

        log.fine("Assert process intance has runing and first two task have arrived");
        TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
        Assert.assertEquals(2, taskQuery.count());

        log.fine("sub-process, activity is all persist in [ACT_RU_EXECUTION] and there's PARENT_ID_ have value.");

        String taskName = workflowAccessor.getTaskNameByDefineId(processDefinitionKey, "subprocess3usertask9");
        Assert.assertEquals("一线经理审核", taskName);
    }

}