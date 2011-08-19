/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.baidu.rigel.service.workflow.api;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

/**
 *
 * @author mengran
 */
public class GetTaskNameByDefineIdTest extends PluggableActivitiTestCase {

    private static final Logger log = Logger.getLogger(GetTaskNameByDefineIdTest.class.getName());
        /* (non-Javadoc)
     * @see org.activiti.engine.impl.test.PluggableActivitiTestCase#initializeProcessEngine()
     */
    @Override
    protected void initializeProcessEngine() {
        if (cachedProcessEngine == null) {
            ClassLoader classLoader = ReflectUtil.getClassLoader();
            Enumeration<URL> resources = null;
            try {
                resources = classLoader.getResources("com/baidu/rigel/service/workflow/activiti.cfg.xml");
            } catch (IOException e) {
                throw new ActivitiException("problem retrieving activiti.cfg.xml resources on the classpath: " + System.getProperty("java.class.path"), e);
            }

            ProcessEngines.retry(resources.nextElement().toString());
            try {
                Field field = ProcessEngines.class.getDeclaredField("isInitialized");
                field.setAccessible(true);
                try {
                    field.setBoolean(ProcessEngines.class, true);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(ContinuousPerformanceTests.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(ContinuousPerformanceTests.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (NoSuchFieldException ex) {
                Logger.getLogger(ContinuousPerformanceTests.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(ContinuousPerformanceTests.class.getName()).log(Level.SEVERE, null, ex);
            }
            cachedProcessEngine = ProcessEngines.getDefaultProcessEngine();
            if (cachedProcessEngine == null) {
                throw new ActivitiException("no default process engine available");
            }
        }
        processEngine = cachedProcessEngine;
    }


    @Deployment(resources = {"com/baidu/rigel/service/workflow/api/pg-support.bpmn20.xml"})
    public void testGetTaskNameByDefineId() throws InterruptedException {

        final WorkflowOperations workflowAccessor = (WorkflowOperations) processEngineConfiguration.getBeans().get("workflowAccessor");

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
        assertEquals(2, taskQuery.count());

        log.fine("sub-process, activity is all persist in [ACT_RU_EXECUTION] and there's PARENT_ID_ have value.");

        String taskName = workflowAccessor.getTaskNameByDefineId(processDefinitionKey, "subprocess3usertask9");
        assertEquals("一线经理审核", taskName);
    }

}