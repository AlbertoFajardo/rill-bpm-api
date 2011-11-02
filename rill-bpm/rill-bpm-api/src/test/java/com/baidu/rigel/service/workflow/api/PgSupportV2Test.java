package com.baidu.rigel.service.workflow.api;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.test.Deployment;

import com.baidu.rigel.service.workflow.api.WorkflowOperations.XStreamSerializeHelper;
import com.baidu.rigel.service.workflow.api.processvar.DummyOrderAudit;

public class PgSupportV2Test extends PluggableActivitiTestCase {

	private static final Logger log = Logger.getLogger(PgSupportV2Test.class
			.getName());

	private WorkflowOperations workflowAccessor;

	public WorkflowOperations getWorkflowAccessor() {
		return workflowAccessor;
	}

	public void setWorkflowAccessor(WorkflowOperations workflowAccessor) {
		this.workflowAccessor = workflowAccessor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.activiti.engine.impl.test.PluggableActivitiTestCase#
	 * initializeProcessEngine()
	 */
	@Override
	protected void initializeProcessEngine() {
		if (cachedProcessEngine == null) {
			ClassLoader classLoader = ReflectUtil.getClassLoader();
			Enumeration<URL> resources = null;
			try {
				resources = classLoader
						.getResources("com/baidu/rigel/service/workflow/activiti.cfg.xml");
			} catch (IOException e) {
				throw new ActivitiException(
						"problem retrieving activiti.cfg.xml resources on the classpath: "
								+ System.getProperty("java.class.path"), e);
			}

			ProcessEngines.retry(resources.nextElement().toString());
			try {
				Field field = ProcessEngines.class
						.getDeclaredField("isInitialized");
				field.setAccessible(true);
				try {
					field.setBoolean(ProcessEngines.class, true);
				} catch (IllegalArgumentException ex) {
					Logger.getLogger(ContinuousPerformanceTests.class.getName())
							.log(Level.SEVERE, null, ex);
				} catch (IllegalAccessException ex) {
					Logger.getLogger(ContinuousPerformanceTests.class.getName())
							.log(Level.SEVERE, null, ex);
				}
			} catch (NoSuchFieldException ex) {
				Logger.getLogger(ContinuousPerformanceTests.class.getName())
						.log(Level.SEVERE, null, ex);
			} catch (SecurityException ex) {
				Logger.getLogger(ContinuousPerformanceTests.class.getName())
						.log(Level.SEVERE, null, ex);
			}
			cachedProcessEngine = ProcessEngines.getDefaultProcessEngine();
			if (cachedProcessEngine == null) {
				throw new ActivitiException(
						"no default process engine available");
			}
		}
		processEngine = cachedProcessEngine;
	}

	@Override
	protected void initializeServices() {
		super.initializeServices();

		// Inject workflow accessor
		this.setWorkflowAccessor((WorkflowOperations) processEngineConfiguration
				.getBeans().get("workflowAccessor"));
	}

	@Deployment(resources = {
			"com/baidu/rigel/service/workflow/api/pg-support_v2.bpmn20.xml",
			"com/baidu/rigel/service/workflow/api/Pg-support-managerAudit_v2.bpmn20.xml",
			"com/baidu/rigel/service/workflow/api/Pg-support-orderValidityAudit_v2.bpmn20.xml",
			"com/baidu/rigel/service/workflow/api/Pg-support-resetOrderReceiptType_v2.bpmn20.xml" })
	public void testPgSupportV2() {

		String processDefinitionKey = "Pg-support_v2";
		Integer orderId = new Random().nextInt();

		DummyOrderAudit orderAudit = new DummyOrderAudit();
		orderAudit.setAuditAction(DummyOrderAudit.AGREE);

		log.log(Level.FINE, "Start process by key{0}], and business key[{1}]",
				new Object[] { processDefinitionKey, orderId });

		// -------------------------------- start Pg-support_v2
		// Start process by KEY
		List<String> taskList = workflowAccessor.createProcessInstance(
				processDefinitionKey, "Rill Meng", orderId.toString(), null);
		assertEquals(2, taskList.size());

		// Test obtain extend attributes logic
		String manageraudit = null;
		for (String taskId : taskList) {
			HashMap<String, String> extendAttributes = workflowAccessor
					.getTaskInstanceExtendAttrs(taskId);
			assertNotNull(extendAttributes);
			assertNotNull(extendAttributes.get(WorkflowTemplate.TASK_ROLE_TAG));
			if (!extendAttributes.get(WorkflowTemplate.TASK_ROLE_TAG).equals(
					"ht_support_khfz_staff")) {
				// manageraudit
				manageraudit = taskId;
				assertEquals("manageraudit/frontManagerAuditInit.action",
						extendAttributes.get(WorkflowTemplate.TASK_FORM_KEY));
				assertEquals("status=2", extendAttributes.get("init_status"));
				assertEquals("12", extendAttributes.get("event"));
				assertEquals("ht_support_khfzb_manager",
						extendAttributes.get(WorkflowTemplate.TASK_ROLE_TAG));
				assertEquals("manageraudit",
						extendAttributes.get(WorkflowTemplate.TASK_DEFINE_ID));
			}
		}

		// Complete manageraudit
		Map<String, String> managerauditWorkflowParams = new HashMap<String, String>();
		managerauditWorkflowParams.put("need_highlevel_audit", "true");
		managerauditWorkflowParams.put("orderAudit",
				XStreamSerializeHelper.serializeXml("orderAudit", orderAudit));
		List<String> managerauditResult = workflowAccessor
				.completeTaskInstance(manageraudit, "manageraudit",
						managerauditWorkflowParams);
		assertEquals(1, managerauditResult.size());
		String seniormanageraudit = managerauditResult.get(0);

		// Complete seniormanageraudit and use exists
		// variables[need_highlevel_audit]
		List<String> seniormanagerauditResult = workflowAccessor
				.completeTaskInstance(seniormanageraudit, "seniormanageraudit",
						null);
		assertEquals(1, seniormanagerauditResult.size());
		String directoraudit = seniormanagerauditResult.get(0);

		// Complete directoraudit and change exists
		// variables[need_highlevel_audit] values
		Map<String, String> directorauditWorkflowParams = new HashMap<String, String>();
		directorauditWorkflowParams.put("need_highlevel_audit", "false");
		// Set to reject
		orderAudit.setAuditAction(DummyOrderAudit.REJECT);
		directorauditWorkflowParams.put("orderAudit",
				XStreamSerializeHelper.serializeXml("orderAudit", orderAudit));
		List<String> directorauditResult = workflowAccessor
				.completeTaskInstance(directoraudit, "directoraudit",
						directorauditWorkflowParams);
		assertEquals(1, directorauditResult.size());
		HashMap<String, String> modorderExtendAttributes = workflowAccessor
				.getTaskInstanceExtendAttrs(directorauditResult.get(0));
		assertEquals("modorder",
				modorderExtendAttributes.get(WorkflowTemplate.TASK_DEFINE_ID));
		String modorder = directorauditResult.get(0);

		// -------------------------------- re-enter Pg-support-managerAudit_v2
		// Complete modorder
		Map<String, String> modorderResultWorkflowParams = new HashMap<String, String>();
		orderAudit.setAuditAction(DummyOrderAudit.AGREE);
		modorderResultWorkflowParams.put("orderAudit",
				XStreamSerializeHelper.serializeXml("orderAudit", orderAudit));
		List<String> modorderResult = workflowAccessor.completeTaskInstance(
				modorder, "modorder", modorderResultWorkflowParams);
		assertEquals(1, modorderResult.size());
		HashMap<String, String> managerauditExtendAttributes = workflowAccessor
				.getTaskInstanceExtendAttrs(modorderResult.get(0));
		assertEquals("manageraudit",
				managerauditExtendAttributes.get(WorkflowTemplate.TASK_DEFINE_ID));
		String manageraudit2 = modorderResult.get(0);
		
		// Complete manageraudit2
		Map<String, String> manageraudit2flowParams = new HashMap<String, String>();
		manageraudit2flowParams.put("need_highlevel_audit", "false");
		orderAudit.setAuditAction(DummyOrderAudit.AGREE);
		manageraudit2flowParams.put("orderAudit",
				XStreamSerializeHelper.serializeXml("orderAudit", orderAudit));
		List<String> manageraudit2Result = workflowAccessor.completeTaskInstance(
				manageraudit2, "manageraudit2", manageraudit2flowParams);
		assertEquals(1, manageraudit2Result.size());
		HashMap<String, String> preauditExtendAttributes = workflowAccessor
				.getTaskInstanceExtendAttrs(manageraudit2Result.get(0));
		assertEquals("preaudit",
				preauditExtendAttributes
						.get(WorkflowTemplate.TASK_DEFINE_ID));
		String preaudit = manageraudit2Result.get(0);

		// Complete preaudit
		List<String> preauditResult = workflowAccessor.completeTaskInstance(
				preaudit, "preaudit", null);
		assertEquals(1, preauditResult.size());
		HashMap<String, String> sendcontractExtendAttributes = workflowAccessor
				.getTaskInstanceExtendAttrs(preauditResult.get(0));
		assertEquals("sendcontract",
				sendcontractExtendAttributes
						.get(WorkflowTemplate.TASK_DEFINE_ID));
		String sendcontract = preauditResult.get(0);

		// Complete sendcontract
		Map<String, String> sendcontractflowParams = new HashMap<String, String>();
		orderAudit.setAuditAction(DummyOrderAudit.AGREE);
		sendcontractflowParams.put("orderAudit",
				XStreamSerializeHelper.serializeXml("orderAudit", orderAudit));
		List<String> sendcontractResult = workflowAccessor
				.completeTaskInstance(sendcontract, "sendcontract", sendcontractflowParams);
		assertEquals(0, sendcontractResult.size());
		String processInstanceId = workflowAccessor.getEngineProcessInstanceIdByBOId(orderId.toString(), null);
		assertNotNull("Root process instance isn't running?", processInstanceId);
		log.info("process instance[" + processInstanceId + "] is running.");
		
		// -------------------------------- Pg-support-resetOrderReceiptType_v2
		
	}

}
