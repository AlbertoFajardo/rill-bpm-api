/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rill.bpm.api;

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

import javax.annotation.Resource;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.rill.bpm.PeerMethodTestHelperTaskExecutionListener;
import org.rill.bpm.api.ThreadLocalResourceHolder;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.activiti.ActivitiAccessor;
import org.rill.bpm.api.processvar.DummyOrderAudit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;


/**
 *
 * @author mengran
 */
@ContextConfiguration(value="classpath:org/rill/bpm/api/activiti.cfg.xml")
@TestExecutionListeners({PeerMethodTestHelperTaskExecutionListener.class})
public class ActivitiOptimisticLockTest extends AbstractJUnit4SpringContextTests {

    private final Logger log = Logger.getLogger(getClass().getName());
    
    public ActivitiOptimisticLockTest() {
    }
    
    @Resource
    private WorkflowOperations workflowAccessor;

    @Deployment(resources = {"org/rill/bpm/api/pg-support.bpmn20.xml"})
    @Test
    public void testOptimisticLock() throws InterruptedException {

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

        // First complete manager audit
        log.fine("First complete manager audit");
        List<Task> taskList = taskQuery.list();
        Task yixianAuditTask = null;
        for (Task t : taskList) {
            log.info(t.getName() + " " + "一线经理审核.equals(t.getName())" + ("一线经理审核".equals(t.getName())));
            if ("一线经理审核".equals(t.getName())) {
                yixianAuditTask = t;
            }
        }
        final Task khfabManagerTask = taskService.createTaskQuery().taskCandidateGroup("ht_support_khfzb_manager").processInstanceId(processInstance.getId()).singleResult();
        log.fine("Task in ACTIVITI5 not override hashcode&equals method, and not ==");
        Assert.assertTrue(!yixianAuditTask.equals(khfabManagerTask));
        Assert.assertNotSame(yixianAuditTask, khfabManagerTask);

        // Pass and not need high level re-audit
        log.entering("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
        final Map<String, Object> workflowParams = new HashMap<String, Object>();
        DummyOrderAudit orderAudit = new DummyOrderAudit();
        workflowParams.put("orderAudit", orderAudit);
        workflowParams.put("need_highlevel_audit", "0");
        log.fine("Complete task and set variables");

        // We complete this task in concurrent mode
        class ConcurrentCompleteTask implements Callable<Boolean> {

            public Boolean call() throws Exception {
                boolean successComplete = true;
                try {
                    log.entering("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
                    workflowAccessor.completeTaskInstance(khfabManagerTask.getId(), "junit", workflowParams);
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
            	Assert.assertEquals(false, executeResult2.get().booleanValue());
            	Assert.assertEquals(false, executeResult3.get().booleanValue());
                log.info("executeResult1 complete task.");
            } else if (executeResult2.get()) {
            	Assert.assertEquals(false, executeResult1.get().booleanValue());
            	Assert.assertEquals(false, executeResult3.get().booleanValue());
                log.info("executeResult2 complete task.");
            } else if (executeResult3.get()) {
            	Assert.assertEquals(false, executeResult2.get().booleanValue());
            	Assert.assertEquals(false, executeResult1.get().booleanValue());
                log.info("executeResult3 complete task.");
            } else {
            	Assert.assertEquals("Not thread complete task?", true, false);
            }
        } catch(ExecutionException ee) {
            ee.printStackTrace();
            Assert.assertEquals(ee.getMessage(), true, false);
        }
    }

}