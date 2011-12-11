/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rill.bpm.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import org.rill.bpm.PeerClassTestHelperTaskExecutionListener;
import org.rill.bpm.api.ThreadLocalResourceHolder;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.activiti.ActivitiAccessor;
import org.rill.bpm.api.extendattr.StatefulDummyTLIStatusHolder;
import org.rill.bpm.api.processvar.DummyOrderAudit;
import org.rill.bpm.api.processvar.DummyReceiptInfo;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.util.ObjectUtils;


/**
 *
 * @author mengran
 */
@ContextConfiguration(value="classpath:org/rill/bpm/api/activiti.cfg.xml")
@TestExecutionListeners({PeerClassTestHelperTaskExecutionListener.class})
public class PgSupportTest extends AbstractJUnit4SpringContextTests {

    private static final Logger log = Logger.getLogger(PgSupportTest.class.getName());
    @Resource
    private WorkflowOperations workflowAccessor;
    
    @Deployment(resources = {"org/rill/bpm/api/pg-support.bpmn20.xml"})
    public void deploy() {
    	
    }
    
    @Test
    public void panguV1() {

    	ActivitiAccessor activitiAccessor = ActivitiAccessor.retrieveActivitiAccessorImpl(workflowAccessor, ActivitiAccessor.class);
        RuntimeService runtimeService = activitiAccessor.getRuntimeService();
        TaskService taskService = activitiAccessor.getTaskService();
    	
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
        Assert.assertEquals(2, taskQuery.count());

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
        Assert.assertTrue(!yixianAuditTask.equals(khfabManagerTask));
        Assert.assertNotSame(yixianAuditTask, khfabManagerTask);

        // Pass and not need high level re-audit
        log.entering("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
        Map<String, Object> workflowParams = new HashMap<String, Object>();
        DummyOrderAudit orderAudit = new DummyOrderAudit();
        workflowParams.put("orderAudit", orderAudit);
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
        Assert.assertNotNull("Dummy task lifecycle interceptor have called.", tliMethodList);
        Assert.assertEquals(tliMethodList.get(0), StatefulDummyTLIStatusHolder.TLI_METHOD.init);
        Assert.assertEquals(tliMethodList.get(1), StatefulDummyTLIStatusHolder.TLI_METHOD.pre);
        Assert.assertEquals(tliMethodList.get(2), StatefulDummyTLIStatusHolder.TLI_METHOD.post);
        Assert.assertEquals(tliMethodList.get(3), StatefulDummyTLIStatusHolder.TLI_METHOD.after);
        log.log(Level.INFO, "Stateful dummy TLI status holder:{0}", ObjectUtils.getDisplayString(StatefulDummyTLIStatusHolder.getStatusHolder()));

        // Check the variables scope
        log.fine("Check the variables scope, taskService.complete() variables will store in process instances scope.");
        Object nhaVariable = runtimeService.getVariable(processInstance.getId(), "need_highlevel_audit");
        Assert.assertNotNull(nhaVariable);

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
        Assert.assertNotNull(preAudit);
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
        Assert.assertNotNull(sendContract);
        Map<String, Object> workflowParams1 = new HashMap<String, Object>();
        DummyReceiptInfo receiptInfo = new DummyReceiptInfo();
        receiptInfo.setReceiptType(DummyReceiptInfo.POST_INVOICE);
        workflowParams1.put("receiptInfo", receiptInfo);
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
        Assert.assertNotNull(completeFinanceInfo);
        log.fine("Complete return-contract and wait finance subprocess's arriving.");
        taskList = taskQuery.list();
        Task returnContract = null;
        for (Task t : taskList) {
            if ("确认合同返还".equals(t.getName())) {
                returnContract = t;
            }
        }
        Assert.assertNotNull(returnContract);
        try {
            log.entering("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
            perTaskStart = System.currentTimeMillis();
            workflowAccessor.completeTaskInstance(returnContract.getId(), "junit", null);
            perTaskTimeCostList.add(System.currentTimeMillis() - perTaskStart);
        } finally {
            log.exiting("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
        }

        ProcessInstance processInstanceCheck = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
        Assert.assertNotNull("Current process should not end at this step.", processInstanceCheck);

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
        Assert.assertNotNull(managerAuditShoukuan);

        log.fine("Set service task expression resovler into process variables");
        Map<String, Object> workflowParams2 = new HashMap<String, Object>();
        try {
            log.entering("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
            perTaskStart = System.currentTimeMillis();
            workflowAccessor.completeTaskInstance(managerAuditShoukuan.getId(), "junit", workflowParams2);
            perTaskTimeCostList.add(System.currentTimeMillis() - perTaskStart);
        } finally {
            log.exiting("PgSupportTest", "completeTaskInstance", ThreadLocalResourceHolder.printAll());
        }

        Task makeOutInvoice = taskService.createTaskQuery().taskCandidateGroup("ht_support_fgscwb_stiffmanager").processInstanceId(processInstance.getId()).singleResult();
        Assert.assertNotNull("Makeout invoice task is null so activityExecution.end method is wrong.", makeOutInvoice);
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
        Assert.assertNotNull(completeFinanceInfo1);
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
        Assert.assertNotNull(receiveMoney);
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
        ActivitiAccessor activitiAccessor = ActivitiAccessor.retrieveActivitiAccessorImpl(workflowAccessor, ActivitiAccessor.class);
        RuntimeService runtimeService = activitiAccessor.getRuntimeService();
        long cnt = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getProcessInstanceId()).count();
        
        Assert.assertEquals(0, cnt);
    }
}
