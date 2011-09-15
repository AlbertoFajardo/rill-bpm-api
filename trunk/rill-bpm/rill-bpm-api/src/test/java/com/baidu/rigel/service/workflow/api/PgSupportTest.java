/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.api;

import com.baidu.rigel.service.workflow.api.extendattr.StatefulDummyTLIStatusHolder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;
import com.baidu.rigel.service.workflow.api.processvar.DummyOrderAudit;
import com.baidu.rigel.service.workflow.api.processvar.DummyReceiptInfo;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import junit.extensions.TestSetup;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.test.TestHelper;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.util.ObjectUtils;

/**
 *
 * @author mengran
 */
public class PgSupportTest extends PluggableActivitiTestCase {

    private static final Logger log = Logger.getLogger(PgSupportTest.class.getName());
    private static volatile String panguDeployId = null;
    private WorkflowOperations workflowAccessor;

    public WorkflowOperations getWorkflowAccessor() {
        return workflowAccessor;
    }

    public void setWorkflowAccessor(WorkflowOperations workflowAccessor) {
        this.workflowAccessor = workflowAccessor;
    }

    public PgSupportTest() {
        this.setName("testPangu");
    }

    public static Test suite() {

        final PgSupportTest pgTest = new PgSupportTest();
        // Decorate test
        TestSetup wrapper = new TestSetup(pgTest) {

            @Override
            protected void setUp() {
                pgTest.deploy();
            }

            @Override
            protected void tearDown() {
                try {
                    pgTest.undeploy();
                } catch (Throwable ex) {
                    log.log(Level.SEVERE, "Fail to call undeploy.", ex);
                }
            }
        };

        return wrapper;
    }

    @Deployment(resources = {"com/baidu/rigel/service/workflow/api/pg-support.bpmn20.xml"})
    public void panguDeploy() {
        // Do nothing
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

    @BeforeClass
    public void deploy() {
        initializeProcessEngine();
        panguDeployId = TestHelper.annotationDeploymentSetUp(processEngine, getClass(), "panguDeploy");
    }

    @AfterClass
    public void undeploy() throws Throwable {

        TestHelper.annotationDeploymentTearDown(processEngine, panguDeployId, getClass(), "panguDeploy");
        assertAndEnsureCleanDb();
        ClockUtil.reset();
    }

    @Override
    protected void initializeServices() {
        super.initializeServices();

        // Inject workflow accessor
        this.setWorkflowAccessor((WorkflowOperations) processEngineConfiguration.getBeans().get("workflowAccessor"));
    }

    @Override
    public void runBare() throws Throwable {
        synchronized (PgSupportTest.class) {
            if (repositoryService == null) {
                initializeServices();
            }
        }

        log.severe(EMPTY_LINE);

        try {
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
//            TestHelper.annotationDeploymentTearDown(processEngine, deploymentId, getClass(), getName());
//            assertAndEnsureCleanDb();
//            ClockUtil.reset();
        }
    }

    /**
     * Runs the bare test sequence.
     * @throws Throwable if any exception is thrown
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

    public void testPangu() {

        long startTime = System.currentTimeMillis();
        List<Long> perTaskTimeCostList = new ArrayList<Long>();
        long perTaskStart = -1;
        Integer orderId = new Random().nextInt();
        String processDefinitionKey = "pg-support";
        log.log(Level.FINE, "Start process by key{0}], and business key[{1}]", new Object[]{processDefinitionKey, orderId});

        try {
            log.entering("PgSupportTest", "createProcessInstance", ThreadLocalResourceHolder.printAll());
            perTaskStart = System.currentTimeMillis();
            // Start process by KEY
            workflowAccessor.createProcessInstance(processDefinitionKey, "Rill Meng", orderId.toString(), null);
            perTaskTimeCostList.add(System.currentTimeMillis() - perTaskStart);
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
            log.log(Level.INFO,"{0}" + " " + "一线经理审核.equals(t.getName()){1}", new Object[]{t.getName(), "一线经理审核".equals(t.getName())});
            if ("一线经理审核".equals(t.getName())) {
                yixianAuditTask = t;
            } else {
                xiugaishoukuanTask = t;
            }
        }
        Task khfabManagerTask = taskService.createTaskQuery().taskCandidateGroup("ht_support_khfzb_manager").processInstanceId(processInstance.getId()).singleResult();
        log.fine("Task in ACTIVITI5 not override hashcode&equals method, and not ==");
        assertTrue(!yixianAuditTask.equals(khfabManagerTask));
        assertNotSame(yixianAuditTask, khfabManagerTask);

        // Pass and not need high level re-audit
        log.entering("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
        Map<String, String> workflowParams = new HashMap<String, String>();
        DummyOrderAudit orderAudit = new DummyOrderAudit();
        workflowParams.put("orderAudit", WorkflowOperations.XStreamSerializeHelper.serializeXml("orderAudit", orderAudit));
        workflowParams.put("need_highlevel_audit", "0");
        log.fine("Complete task and set variables");
        try {
            log.entering("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
            perTaskStart = System.currentTimeMillis();
            workflowAccessor.completeTaskInstance(khfabManagerTask.getId(), "junit", workflowParams);
            perTaskTimeCostList.add(System.currentTimeMillis() - perTaskStart);
        } finally {
            log.exiting("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
        }
        // task lifecycle interceptor called
        List<StatefulDummyTLIStatusHolder.TLI_METHOD> tliMethodList = StatefulDummyTLIStatusHolder.getStatusHolder().get(khfabManagerTask.getId());
        assertNotNull("Dummy task lifecycle interceptor have called.", tliMethodList);
        assertEquals(tliMethodList.get(0), StatefulDummyTLIStatusHolder.TLI_METHOD.init);
        assertEquals(tliMethodList.get(1), StatefulDummyTLIStatusHolder.TLI_METHOD.pre);
        assertEquals(tliMethodList.get(2), StatefulDummyTLIStatusHolder.TLI_METHOD.post);
        assertEquals(tliMethodList.get(3), StatefulDummyTLIStatusHolder.TLI_METHOD.after);
        log.log(Level.INFO, "Stateful dummy TLI status holder:{0}", ObjectUtils.getDisplayString(StatefulDummyTLIStatusHolder.getStatusHolder()));

        // Check the variables scope
        log.fine("Check the variables scope, taskService.complete() variables will store in process instances scope.");
        Object nhaVariable = runtimeService.getVariable(processInstance.getId(), "need_highlevel_audit");
        assertNotNull(nhaVariable);

        // Complete manager-audit and arrive pre-audit
        log.fine("Arrive pre-audit");
        taskList = taskQuery.list();
        Task preAudit = null;
        for (Task t : taskList) {
            if (!xiugaishoukuanTask.getName().equals(t.getName())) {
                preAudit = t;
            } else {
            }
        }
        assertNotNull(preAudit);
        try {
            log.entering("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
            perTaskStart = System.currentTimeMillis();
            workflowAccessor.completeTaskInstance(preAudit.getId(), "junit", workflowParams);
            perTaskTimeCostList.add(System.currentTimeMillis() - perTaskStart);
        } finally {
            log.exiting("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
        }

        // Complete pre-audit and arrive send-contract
        log.fine("Arrive send-contract");
        taskList = taskQuery.list();
        Task sendContract = null;
        for (Task t : taskList) {
            if (!xiugaishoukuanTask.getName().equals(t.getName())) {
                sendContract = t;
            }
        }
        assertNotNull(sendContract);
        Map<String, String> workflowParams1 = new HashMap<String, String>();
        DummyReceiptInfo receiptInfo = new DummyReceiptInfo();
        receiptInfo.setReceiptType(DummyReceiptInfo.POST_INVOICE);
        workflowParams1.put("receiptInfo", WorkflowOperations.XStreamSerializeHelper.serializeXml("receiptInfo", receiptInfo));
        try {
            log.entering("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
            perTaskStart = System.currentTimeMillis();
            workflowAccessor.completeTaskInstance(sendContract.getId(), "junit", workflowParams1);
            perTaskTimeCostList.add(System.currentTimeMillis() - perTaskStart);
        } finally {
            log.exiting("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
        }

        // Complete pre-audit and arrive send-contract
        log.fine("Arrive return-contract/finance subprocess");
        taskList = taskQuery.list();
        Task completeFinanceInfo = null;
        for (Task t : taskList) {
            if ("完善财务信息".equals(t.getName())) {
                completeFinanceInfo = t;
            }
        }
        assertNotNull(completeFinanceInfo);
        log.fine("Complete return-contract and wait finance subprocess's arriving.");
        taskList = taskQuery.list();
        Task returnContract = null;
        for (Task t : taskList) {
            if ("确认合同返还".equals(t.getName())) {
                returnContract = t;
            }
        }
        assertNotNull(returnContract);
        try {
            log.entering("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
            perTaskStart = System.currentTimeMillis();
            workflowAccessor.completeTaskInstance(returnContract.getId(), "junit", null);
            perTaskTimeCostList.add(System.currentTimeMillis() - perTaskStart);
        } finally {
            log.exiting("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
        }

        ProcessInstance processInstanceCheck = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
        assertNotNull("Current process should not end at this step.", processInstanceCheck);

        log.fine("Complete xiugaishoukuan task, use exists process variables");
        try {
            log.entering("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
            perTaskStart = System.currentTimeMillis();
            workflowAccessor.completeTaskInstance(xiugaishoukuanTask.getId(), "junit", null);
            perTaskTimeCostList.add(System.currentTimeMillis() - perTaskStart);
        } finally {
            log.exiting("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
        }
        Task managerAuditShoukuan = taskService.createTaskQuery().taskCandidateGroup("ht_support_khfzb_manager").processInstanceId(processInstance.getId()).singleResult();
        assertNotNull(managerAuditShoukuan);

        log.fine("Set service task expression resovler into process variables");
        Map<String, String> workflowParams2 = new HashMap<String, String>();
        try {
            log.entering("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
            perTaskStart = System.currentTimeMillis();
            workflowAccessor.completeTaskInstance(managerAuditShoukuan.getId(), "junit", workflowParams2);
            perTaskTimeCostList.add(System.currentTimeMillis() - perTaskStart);
        } finally {
            log.exiting("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
        }

        Task makeOutInvoice = taskService.createTaskQuery().taskCandidateGroup("ht_support_fgscwb_stiffmanager").processInstanceId(processInstance.getId()).singleResult();
        assertNotNull("Makeout invoice task is null so activityExecution.end method is wrong.", makeOutInvoice);
        log.fine("Complete makeOutInvoice task");
        try {
            log.entering("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
            perTaskStart = System.currentTimeMillis();
            workflowAccessor.completeTaskInstance(makeOutInvoice.getId(), "junit", null);
            perTaskTimeCostList.add(System.currentTimeMillis() - perTaskStart);
        } finally {
            log.exiting("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
        }

        // Complete pre-audit and arrive send-contract
        Task completeFinanceInfo1 = taskQuery.singleResult();
        assertNotNull(completeFinanceInfo1);
        log.fine("Complete completeFinanceInfo1.");
        try {
            log.entering("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
            perTaskStart = System.currentTimeMillis();
            workflowAccessor.completeTaskInstance(completeFinanceInfo1.getId(), "junit", null);
            perTaskTimeCostList.add(System.currentTimeMillis() - perTaskStart);
        } finally {
            log.exiting("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
        }

        // Do end task
        endProcess(taskQuery, processInstance, startTime, perTaskTimeCostList);
    }

    public void endProcess(TaskQuery taskQuery, ProcessInstance processInstance,
            long startTime, List<Long> perTaskTimeCostList) {

        // Complete pre-audit and arrive send-contract
        Task receiveMoney = taskQuery.singleResult();
        assertNotNull(receiveMoney);
        log.fine("Complete receive money and end the process.");
        try {
            log.entering("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
            long perTaskStart = System.currentTimeMillis();
            workflowAccessor.completeTaskInstance(receiveMoney.getId(), "junit", null);
            perTaskTimeCostList.add(System.currentTimeMillis() - perTaskStart);
        } finally {
            log.exiting("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
        }

        // Assert process have ended.
        assertProcessEnded(processInstance.getId());
    }
}
