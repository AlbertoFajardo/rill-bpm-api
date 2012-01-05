package org.rill.bpm.api;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.imageio.stream.FileImageOutputStream;

import org.activiti.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.rill.bpm.PeerMethodTestHelperTaskExecutionListener;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.WorkflowTemplate;
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
			"org/rill/bpm/api/Pg-support-receiptFounds_v2.bpmn20.xml"
			 })
	@Test
	public void testPgSupportV2() {

		String processDefinitionKey = "Pg-support_v2";
		Integer orderId = new Random().nextInt();

		DummyOrderAudit orderAudit = new DummyOrderAudit();
		orderAudit.setAuditAction(DummyOrderAudit.AGREE);

		logger.info("Start process by key " + processDefinitionKey +"], and business key[" + orderId +"]");

		// -------------------------------- start Pg-support_v2
		// Start process by KEY
		List<String> taskList = workflowAccessor.createProcessInstance(
				processDefinitionKey, "Rill Meng", orderId.toString(), null);
		Assert.assertEquals(2, taskList.size());

		// Test obtain extend attributes logic
		String manageraudit = null;
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
		for (String taskId : sendcontractResult) {
			HashMap<String, String> extendAttributes = workflowAccessor
					.getTaskInstanceInformations(taskId);
			logger.info(extendAttributes);
		}
		
		String processInstanceId = workflowAccessor.getEngineProcessInstanceIdByBOId(orderId.toString(), null);
		Assert.assertNotNull("Root process instance isn't running?", processInstanceId);
		logger.info("process instance[" + processInstanceId + "] is running.");
		
		// -------------------------------- Pg-support-resetOrderReceiptType_v2
		
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
	            Logger.getLogger(PgSupportV2Test.class.getName()).log(Level.SEVERE, null, ex);
	            Assert.assertEquals("Can not generate process " + entry.getKey() + " diagram image.", true, false);
	        }
		}
		
	}

}
