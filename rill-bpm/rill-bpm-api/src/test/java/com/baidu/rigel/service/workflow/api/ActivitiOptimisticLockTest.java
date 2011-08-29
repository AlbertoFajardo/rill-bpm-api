/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.baidu.rigel.service.workflow.api;

import com.baidu.rigel.service.workflow.api.processvar.DummyOrderAudit;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

/**
 *
 * @author mengran
 */
public class ActivitiOptimisticLockTest extends PluggableActivitiTestCase {

    private static final Logger log = Logger.getLogger(ActivitiOptimisticLockTest.class.getName());
    
    public ActivitiOptimisticLockTest() {
    }

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
    public void testOptimisticLock() throws InterruptedException {

        final WorkflowOperations workflowAccessor = (WorkflowOperations) processEngineConfiguration.getBeans().get("workflowAccessor");

        Integer orderId = new Random().nextInt();
        String processDefinitionKey = "pg-support";
        log.fine("Start process by key" + processDefinitionKey + "], and business key[" + orderId + "]");

        try {
            log.entering("PgSupportTest", "createProcessInstance", ThreadLocalResourceHolder.printAll());
            // Start process by KEY
            WorkflowOperations.CreateProcessInstanceDto createProcessInstanceDto = new WorkflowOperations.CreateProcessInstanceDto(processDefinitionKey, "Rill Meng", orderId.toString(), null);
            workflowAccessor.createProcessInstance(createProcessInstanceDto);
        } finally {
            log.exiting("PgSupportTest", "createProcessInstance", ThreadLocalResourceHolder.printAll());
        }

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(orderId.toString(), processDefinitionKey).singleResult();

        log.fine("Assert process intance has runing and first two task have arrived");
        TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
        assertEquals(2, taskQuery.count());

        log.fine("sub-process, activity is all persist in [ACT_RU_EXECUTION] and there's PARENT_ID_ have value.");

        // First complete manager audit
        log.fine("First complete manager audit");
        List<Task> taskList = taskQuery.list();
        Task yixianAuditTask = null;
        Task xiugaishoukuanTask = null;
        for (Task t : taskList) {
            log.info(t.getName() + " " + "一线经理审核.equals(t.getName())" + ("一线经理审核".equals(t.getName())));
            if ("一线经理审核".equals(t.getName())) {
                yixianAuditTask = t;
            } else {
                xiugaishoukuanTask = t;
            }
        }
        final Task khfabManagerTask = taskService.createTaskQuery().taskCandidateGroup("ht_support_khfzb_manager").processInstanceId(processInstance.getId()).singleResult();
        log.fine("Task in ACTIVITI5 not override hashcode&equals method, and not ==");
        assertTrue(!yixianAuditTask.equals(khfabManagerTask));
        assertNotSame(yixianAuditTask, khfabManagerTask);

        // Pass and not need high level re-audit
        log.entering("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
        final Map<String, String> workflowParams = new HashMap<String, String>();
        DummyOrderAudit orderAudit = new DummyOrderAudit();
        workflowParams.put("orderAudit", WorkflowOperations.XStreamSerializeHelper.serializeXml("orderAudit", orderAudit));
        workflowParams.put("need_highlevel_audit", "0");
        log.fine("Complete task and set variables");

        // We complete this task in concurrent mode
        class ConcurrentCompleteTask implements Callable<Boolean> {

            public Boolean call() throws Exception {
                boolean successComplete = true;
                try {
                    log.entering("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
                    WorkflowOperations.CompleteTaskInstanceDto completeTaskInstanceDto = new WorkflowOperations.CompleteTaskInstanceDto(khfabManagerTask.getId(), "junit", workflowParams);
                    workflowAccessor.completeTaskInstance(completeTaskInstanceDto);
                } catch (Throwable t) {
                    successComplete = false;
                    log.info(t.getMessage());
                    log.exiting("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
                }
                return successComplete;
            }
        }
        ExecutorService pool = Executors.newFixedThreadPool(3);
        Future<Boolean> executeResult1 = pool.submit(new ConcurrentCompleteTask());
        Future<Boolean> executeResult2 = pool.submit(new ConcurrentCompleteTask());
        Future<Boolean> executeResult3 = pool.submit(new ConcurrentCompleteTask());

        // Only one result is true
        try {
            if (executeResult1.get()) {
                assertEquals(false, executeResult2.get().booleanValue());
                assertEquals(false, executeResult3.get().booleanValue());
                log.info("executeResult1 complete task.");
            } else if (executeResult2.get()) {
                assertEquals(false, executeResult1.get().booleanValue());
                assertEquals(false, executeResult3.get().booleanValue());
                log.info("executeResult2 complete task.");
            } else if (executeResult3.get()) {
                assertEquals(false, executeResult2.get().booleanValue());
                assertEquals(false, executeResult1.get().booleanValue());
                log.info("executeResult3 complete task.");
            } else {
                assertEquals("Not thread complete task?", true, false);
            }
        } catch(ExecutionException ee) {
            ee.printStackTrace();
            assertEquals(ee.getMessage(), true, false);
        }
    }

}