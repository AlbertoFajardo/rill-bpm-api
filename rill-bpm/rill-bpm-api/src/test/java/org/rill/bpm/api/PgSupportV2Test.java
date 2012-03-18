package org.rill.bpm.api;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.annotation.Resource;
import javax.imageio.stream.FileImageOutputStream;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.test.Deployment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.rill.bpm.PeerMethodTestHelperTaskExecutionListener;
import org.rill.bpm.api.activiti.ActivitiAccessor;
import org.rill.bpm.api.activiti.bpmndiagram.ProcessMonitorChartInfoHelper;
import org.rill.bpm.api.activiti.bpmndiagram.ProcessMonitorChartInfoHelper.ChartInfo;
import org.rill.bpm.api.processvar.DummyOrder;
import org.rill.bpm.api.processvar.DummyOrderAudit;
import org.rill.bpm.api.processvar.DummyReceiptInfo;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;


@ContextConfiguration(value="classpath:org/rill/bpm/api/activiti.cfg.xml")
@TestExecutionListeners({PeerMethodTestHelperTaskExecutionListener.class})
public class PgSupportV2Test extends AbstractJUnit4SpringContextTests {

	protected final Log logger = LogFactory.getLog(getClass().getName());
	
	@Resource
	private WorkflowOperations workflowAccessor;

	public WorkflowOperations getWorkflowAccessor() {
		return workflowAccessor;
	}

	public void setWorkflowAccessor(WorkflowOperations workflowAccessor) {
		this.workflowAccessor = workflowAccessor;
	}
	
	@Resource
	private ProcessMonitorChartInfoHelper processMonitorChartInfoHelper;
	

	public final ProcessMonitorChartInfoHelper getProcessMonitorChartInfoHelper() {
		return processMonitorChartInfoHelper;
	}

	public final void setProcessMonitorChartInfoHelper(
			ProcessMonitorChartInfoHelper processMonitorChartInfoHelper) {
		this.processMonitorChartInfoHelper = processMonitorChartInfoHelper;
	}

	@Deployment(resources = {
			"org/rill/bpm/api/pg-support_v2.bpmn20.xml",
			"org/rill/bpm/api/Pg-support-managerAudit_v2.bpmn20.xml",
			"org/rill/bpm/api/Pg-support-orderValidityAudit_v2.bpmn20.xml",
			"org/rill/bpm/api/Pg-support-resetOrderReceiptType_v2.bpmn20.xml",
			"org/rill/bpm/api/Pg-support-addmoney_v2.bpmn20.xml",
			"org/rill/bpm/api/Pg-support-contractQualification_v2.bpmn20.xml",
			"org/rill/bpm/api/Pg-support-gift_v2.bpmn20.xml",
			"org/rill/bpm/api/Pg-support-openaccount_v2.bpmn20.xml",
			"org/rill/bpm/api/Pg-support-orderPrint_v2.bpmn20.xml",
			"org/rill/bpm/api/Pg-support-promotionsExpires-contract_v2.bpmn20.xml",
			"org/rill/bpm/api/Pg-support-receiptFounds_v2.bpmn20.xml",
			"org/rill/bpm/api/Pg-support-managerAudit_agt_v2.bpmn20.xml"
			 })
	@Test
	public void testPgSupportV2() {

		String processDefinitionKey = "Pg-support_v2";
		Integer orderId = new Random().nextInt();
		String processStarter = "Rill Meng";

		DummyOrderAudit orderAudit = new DummyOrderAudit();
		orderAudit.setAuditAction(DummyOrderAudit.AGREE);

		logger.info("Start process by key " + processDefinitionKey +"], and business key[" + orderId +"]");

		// -------------------------------- start Pg-support_v2
		// Start process by KEY
		Map<String, Object> startProcessParams = new HashMap<String, Object>();
		startProcessParams.put("biz_mode", "0");
		List<String> taskList = workflowAccessor.createProcessInstance(
				processDefinitionKey, processStarter, orderId.toString(), startProcessParams);
		Assert.assertEquals(2, taskList.size());
		
		// Check process starter info at 2012-02-07
		String processInstanceId = workflowAccessor.getEngineProcessInstanceIdByBOId(orderId.toString(), null);
		for (int i = 0; i < 2; i++) {
			processInstanceId = workflowAccessor.getEngineProcessInstanceIdByBOId(orderId.toString(), null);
		}
		ActivitiAccessor activitiAccessor = ActivitiAccessor.retrieveActivitiAccessorImpl(workflowAccessor, ActivitiAccessor.class);
		HistoricProcessInstance hisPi = activitiAccessor.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		Assert.assertEquals(processStarter, hisPi.getStartUserId());
		Assert.assertEquals(null, Authentication.getAuthenticatedUserId());
		User persistentUser = activitiAccessor.getIdentityService().createUserQuery().userId(processStarter).singleResult();
		Assert.assertEquals(processStarter, persistentUser.getId());

		// Test obtain extend attributes logic
		String manageraudit = null;
		String resetOrderReceiptTypeTaskId = null;
		for (String taskId : taskList) {
			HashMap<String, String> extendAttributes = workflowAccessor
					.getTaskInstanceInformations(taskId);
			Assert.assertNotNull(extendAttributes);
			Assert.assertNotNull(extendAttributes.get(WorkflowTemplate.TASK_ROLE_TAG));
			if (!extendAttributes.get(WorkflowTemplate.TASK_ROLE_TAG).equals(
					"ht_support_khfz_staff")) {
				// manageraudit
				manageraudit = taskId;
				Assert.assertEquals("manageraudit/frontManagerAuditInit.action",
						extendAttributes.get(WorkflowTemplate.TASK_FORM_KEY));
				Assert.assertEquals("status=2", extendAttributes.get("init_status"));
				Assert.assertEquals("12", extendAttributes.get("event"));
				Assert.assertEquals("ht_support_khfzb_manager",
						extendAttributes.get(WorkflowTemplate.TASK_ROLE_TAG));
				Assert.assertEquals("manageraudit",
						extendAttributes.get(WorkflowTemplate.TASK_DEFINE_ID));
			} else {
				resetOrderReceiptTypeTaskId = taskId;
			}
		}

		// Complete manageraudit
		Map<String, Object> managerauditWorkflowParams = new HashMap<String, Object>();
		managerauditWorkflowParams.put("need_highlevel_audit", "true");
		managerauditWorkflowParams.put("orderAudit", orderAudit);
		List<String> managerauditResult = workflowAccessor
				.completeTaskInstance(manageraudit, "manageraudit",
						managerauditWorkflowParams);
		Assert.assertEquals(1, managerauditResult.size());
		String seniormanageraudit = managerauditResult.get(0);

		// Complete seniormanageraudit and use exists
		// variables[need_highlevel_audit]
		List<String> seniormanagerauditResult = workflowAccessor
				.completeTaskInstance(seniormanageraudit, "seniormanageraudit",
						null);
		Assert.assertEquals(1, seniormanagerauditResult.size());
		String directoraudit = seniormanagerauditResult.get(0);

		// Complete directoraudit and change exists
		// variables[need_highlevel_audit] values
		Map<String, Object> directorauditWorkflowParams = new HashMap<String, Object>();
		directorauditWorkflowParams.put("need_highlevel_audit", "false");
		// Set to reject
		orderAudit.setAuditAction(DummyOrderAudit.REJECT);
		directorauditWorkflowParams.put("orderAudit", orderAudit);
		List<String> directorauditResult = workflowAccessor
				.completeTaskInstance(directoraudit, "directoraudit",
						directorauditWorkflowParams);
		Assert.assertEquals(1, directorauditResult.size());
		HashMap<String, String> modorderExtendAttributes = workflowAccessor
				.getTaskInstanceInformations(directorauditResult.get(0));
		Assert.assertEquals("modorder",
				modorderExtendAttributes.get(WorkflowTemplate.TASK_DEFINE_ID));
		String modorder = directorauditResult.get(0);

		// -------------------------------- re-enter Pg-support-managerAudit_v2
		// Complete modorder
		Map<String, Object> modorderResultWorkflowParams = new HashMap<String, Object>();
		orderAudit.setAuditAction(DummyOrderAudit.AGREE);
		modorderResultWorkflowParams.put("orderAudit", orderAudit);
		List<String> modorderResult = workflowAccessor.completeTaskInstance(
				modorder, "modorder", modorderResultWorkflowParams);
		Assert.assertEquals(1, modorderResult.size());
		HashMap<String, String> managerauditExtendAttributes = workflowAccessor
				.getTaskInstanceInformations(modorderResult.get(0));
		Assert.assertEquals("manageraudit",
				managerauditExtendAttributes.get(WorkflowTemplate.TASK_DEFINE_ID));
		String manageraudit2 = modorderResult.get(0);
		
		// Complete manageraudit2
		Map<String, Object> manageraudit2flowParams = new HashMap<String, Object>();
		manageraudit2flowParams.put("need_highlevel_audit", "false");
		orderAudit.setAuditAction(DummyOrderAudit.AGREE);
		manageraudit2flowParams.put("orderAudit", orderAudit);
		List<String> manageraudit2Result = workflowAccessor.completeTaskInstance(
				manageraudit2, "manageraudit2", manageraudit2flowParams);
		Assert.assertEquals(1, manageraudit2Result.size());
		HashMap<String, String> preauditExtendAttributes = workflowAccessor
				.getTaskInstanceInformations(manageraudit2Result.get(0));
		Assert.assertEquals("preaudit",
				preauditExtendAttributes
						.get(WorkflowTemplate.TASK_DEFINE_ID));
		String preaudit = manageraudit2Result.get(0);

		// Complete preaudit
		List<String> preauditResult = workflowAccessor.completeTaskInstance(
				preaudit, "preaudit", null);
		Assert.assertEquals(1, preauditResult.size());
		HashMap<String, String> sendcontractExtendAttributes = workflowAccessor
				.getTaskInstanceInformations(preauditResult.get(0));
		Assert.assertEquals("sendcontract",
				sendcontractExtendAttributes
						.get(WorkflowTemplate.TASK_DEFINE_ID));
		String sendcontract = preauditResult.get(0);

		// Complete sendcontract
		Map<String, Object> sendcontractflowParams = new HashMap<String, Object>();
		DummyOrder order = new DummyOrder();
		order.setIsNeedGift(DummyOrder.XIAN_TI);
		DummyReceiptInfo receiptInfo = new DummyReceiptInfo();
		receiptInfo.setReceiptType(DummyReceiptInfo.PRE_INVOICE);
		orderAudit.setAuditAction(DummyOrderAudit.AGREE);
		sendcontractflowParams.put("orderAudit", orderAudit);
		sendcontractflowParams.put("order", order);
		sendcontractflowParams.put("receiptInfo", receiptInfo);
		
		List<String> sendcontractResult = workflowAccessor
				.completeTaskInstance(sendcontract, "sendcontract", sendcontractflowParams);
		// contract finance gift
		Assert.assertEquals(3, sendcontractResult.size());
		String writereceipt = null;
		String auditSendGift = null;
		for (String taskId : sendcontractResult) {
			HashMap<String, String> extendAttributes = workflowAccessor
					.getTaskInstanceInformations(taskId);
			if ("writereceipt".equals(extendAttributes.get(WorkflowOperations.TaskInformations.TASK_TAG.name()))) {
				writereceipt = taskId;
			}
			if ("auditAndSendGift".equals(extendAttributes.get(WorkflowOperations.TaskInformations.TASK_TAG.name()))) {
				auditSendGift = taskId;
			}
		}
		// Fix audit gift Lock timeout
		Map<String, Object> auditGiftSendParams = new HashMap<String, Object>();
		orderAudit.setAuditAction(DummyOrderAudit.AGREE);
		auditGiftSendParams.put("orderAudit", orderAudit);
		List<String> auditGiftSend = workflowAccessor.completeTaskInstance(auditSendGift, "RillMeng", auditGiftSendParams);
		Assert.assertEquals(0, auditGiftSend.size());
		
		processInstanceId = workflowAccessor.getEngineProcessInstanceIdByBOId(orderId.toString(), null);
		Assert.assertNotNull("Root process instance isn't running?", processInstanceId);
		logger.info("process instance[" + processInstanceId + "] is running.");
		
		// -------------------------------- Pg-support-resetOrderReceiptType_v2
		List<String> auditResetOrderReceiptType = workflowAccessor.completeTaskInstance(resetOrderReceiptTypeTaskId, "RillMeng", null);
		Map<String, Object> auditResetOrderReceiptTypeMap = new HashMap<String, Object>();
		orderAudit.setAuditAction(DummyOrderAudit.AGREE);
		auditResetOrderReceiptTypeMap.put("orderAudit", orderAudit);
		auditResetOrderReceiptTypeMap.put("need_highlevel_audit", "false");
		auditResetOrderReceiptType = workflowAccessor.completeTaskInstance(auditResetOrderReceiptType.get(0), "RillMeng", auditResetOrderReceiptTypeMap);
		Assert.assertEquals(0, auditResetOrderReceiptType.size());
		
		Map<String, Object> goBackMap = new HashMap<String, Object>();
		goBackMap.put("__go_back", "true");
		receiptInfo.setReceiptType(DummyReceiptInfo.POST_INVOICE);
		goBackMap.put("receiptInfo", receiptInfo);
		List<String> afterGoBack = workflowAccessor.completeTaskInstance(writereceipt, "auditResetOrderReceiptType", goBackMap);
		HashMap<String, String> extendAttributes = workflowAccessor
				.getTaskInstanceInformations(afterGoBack.get(0));
		Assert.assertEquals("completefinanceinfo", extendAttributes.get(WorkflowOperations.TaskInformations.TASK_TAG.name()));
		Assert.assertEquals(1, afterGoBack.size());
		// Complete finance info
		goBackMap = new HashMap<String, Object>();
		goBackMap.put("orderAudit", orderAudit);
		goBackMap.put("activity_out_date", "false");
		goBackMap.put("finance_info_ok", "true");
		goBackMap.put("biz_mode", "1");
		afterGoBack = workflowAccessor.completeTaskInstance(afterGoBack.get(0), "GoBackIsFalseTest", goBackMap);
		extendAttributes = workflowAccessor
				.getTaskInstanceInformations(afterGoBack.get(0));
		Assert.assertEquals("agt_manageraudit", extendAttributes.get(WorkflowOperations.TaskInformations.TASK_TAG.name()));
		goBackMap = new HashMap<String, Object>();
		orderAudit.setAuditAction(DummyOrderAudit.REJECT);
		goBackMap.put("orderAudit", orderAudit);
		goBackMap.put("need_highlevel_audit", "false");
		afterGoBack = workflowAccessor.completeTaskInstance(afterGoBack.get(0), "bizModeRejectTest", goBackMap);
		extendAttributes = workflowAccessor
				.getTaskInstanceInformations(afterGoBack.get(0));
		Assert.assertEquals("completefinanceinfo", extendAttributes.get(WorkflowOperations.TaskInformations.TASK_TAG.name()));
		
		
		// -------------------------------- Test extend mappings
		activitiAccessor.runExtraCommand(new Command<Void>() {

			@Override
			public Void execute(CommandContext commandContext) {
				Date[] startAndEndDate = org.rill.utils.DateUtils.getDayStartAndEndDate(new Date());
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put("timeBegin", startAndEndDate[0]);
				parameterMap.put("timeEnd", startAndEndDate[1]);
				Object runningResult = commandContext.getDbSqlSession().selectOne("selectDayRunningRootHistoricProcessInstanceCnt", parameterMap);
				Assert.assertEquals(1L, runningResult);
				return null;
			}
		});
		// -------------------------------- Retrieve chart informations
		Map<String, ChartInfo> allChartInfos = getProcessMonitorChartInfoHelper().getMonitorChartInfo(processInstanceId);
//		Assert.assertEquals(5, allChartInfos.size());
		
		for (Entry<String, ChartInfo> entry : allChartInfos.entrySet()) {
	        try {
	            File tmpImage = File.createTempFile("processDefinitionKey" + entry.getKey(), ".png");
	            FileImageOutputStream fios = new FileImageOutputStream(tmpImage);
	            fios.write(entry.getValue().getDiagramBytes());
	            fios.flush();
	            fios.close();
	            fios = null;
	        } catch (IOException ex) {
	        	logger.error("Exception occurred when try to generate png", ex);
	            Assert.assertEquals("Can not generate process " + entry.getKey() + " diagram image.", true, false);
	        }
		}
		
	}

}
