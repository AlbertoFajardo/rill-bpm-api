/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.ws.api.activiti;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.util.Assert;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.baidu.rigel.service.workflow.api.WorkflowOperations;
import com.baidu.rigel.service.workflow.api.activiti.ActivitiAccessor;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;
import com.baidu.rigel.service.workflow.ws.api.RemoteWorkflowOperations;
import com.sun.xml.ws.api.tx.at.Transactional;
import com.sun.xml.ws.api.tx.at.Transactional.Version;

/**
 * Web Service API for work flow.
 * <p>
 * Use @com.sun.xml.ws.api.tx.at.Transactional to mark some WS method is under
 * WS-AT specification.
 * 
 * @author mengran
 */
@WebService
public class RemoteActivitiTemplate implements RemoteWorkflowOperations {

	// Must use getter method!
	private WorkflowOperations workflowAccessor;
	private ActivitiAccessor activitiAccessor;
	private AtomicBoolean needRetrieve = new AtomicBoolean(true);

	@Resource
	private WebServiceContext context;

	private WorkflowOperations getWorkflowAccessor() {
		if (needRetrieve.compareAndSet(true, false)) {
			workflowAccessor = WebApplicationContextUtils
					.getRequiredWebApplicationContext(
							(ServletContext) context.getMessageContext().get(
									MessageContext.SERVLET_CONTEXT)).getBean(
							"workflowAccessor", WorkflowOperations.class);

			if (workflowAccessor instanceof SpringProxy) {
				Object targetSource;
				try {
					targetSource = ((Advised) workflowAccessor)
							.getTargetSource().getTarget();
					while (targetSource instanceof SpringProxy) {
						targetSource = ((Advised) targetSource)
								.getTargetSource().getTarget();
					}
				} catch (Exception e) {
					throw new ProcessException(e);
				}

				activitiAccessor = ((ActivitiAccessor) (targetSource));
			} else {
				activitiAccessor = ((ActivitiAccessor) workflowAccessor);
			}
		}

		Assert.notNull(workflowAccessor);
		return workflowAccessor;
	}

	@Transactional(version = Version.WSAT10)
	public RemoteWorkflowResponse createProcessInstance(
			CreateProcessInstanceDto createProcessInstanceDto)
			throws ProcessException {

		// Delegate this operations
		final List<String> tasks = getWorkflowAccessor().createProcessInstance(
				createProcessInstanceDto.getProcessDefinitionKey(),
				createProcessInstanceDto.getProcessStarter(),
				createProcessInstanceDto.getBusinessObjectId(),
				createProcessInstanceDto.getStartParams());

		final String engineProcessInstanceId = activitiAccessor.getEngineProcessInstanceIdByBOId(
				createProcessInstanceDto.getBusinessObjectId(), createProcessInstanceDto.getProcessDefinitionKey());

		// Directly use create process instance API means root process
		return new RemoteWorkflowResponse(engineProcessInstanceId,
				createProcessInstanceDto.getBusinessObjectId(),
				createProcessInstanceDto.getProcessDefinitionKey(), tasks, engineProcessInstanceId, false);
	}

	@Transactional(version = Version.WSAT10)
	public RemoteWorkflowResponse completeTaskInstance(
			final CompleteTaskInstanceDto completeTaskInstanceDto)
			throws ProcessException {

		return activitiAccessor
				.runExtraCommand(new Command<RemoteWorkflowResponse>() {

					@Override
					public RemoteWorkflowResponse execute(
							CommandContext commandContext) {

						String engineProcessInstanceId = commandContext
								.getTaskManager()
								.findTaskById(
										completeTaskInstanceDto
												.getEngineTaskInstanceId())
								.getProcessInstanceId();
						ExecutionEntity ee = commandContext
								.getExecutionManager().findExecutionById(
										engineProcessInstanceId);
						String businessKey = ee.getBusinessKey();
						ProcessDefinitionEntity pde = commandContext
								.getProcessDefinitionManager()
								.findLatestProcessDefinitionById(
										ee.getProcessDefinitionId());
						String processDefinitionKey = pde.getKey();
						
						// Cache root process instance ID
						String rootProcessInstanceId = activitiAccessor.obtainRootProcess(engineProcessInstanceId, true);
						
						// Delegate this operations
						final List<String> tasks = getWorkflowAccessor().completeTaskInstance(
								completeTaskInstanceDto.getEngineTaskInstanceId(),
								completeTaskInstanceDto.getOperator(),
								completeTaskInstanceDto.getWorkflowParams());
						
						ProcessInstance processInstance = activitiAccessor.getRuntimeService().createProcessInstanceQuery().processInstanceId(engineProcessInstanceId).singleResult();
						return new RemoteWorkflowResponse(
								engineProcessInstanceId, businessKey,
								processDefinitionKey, tasks, rootProcessInstanceId, processInstance == null);
					}

				});

	}
	
	@Override
	@Transactional(version = Version.WSAT10)
	public void deleteProcessInstance(String engineProcessInstanceId,
			String reason) throws ProcessException {

		// Unique instance exists
		if (activitiAccessor == null) {
			getWorkflowAccessor();
		}
		RuntimeService runtimeService = activitiAccessor.getRuntimeService();
		// Do delete operation
		runtimeService.deleteProcessInstance(engineProcessInstanceId, reason);
	}
	
	// ----------------------------------------- Read API as below ----------//

	@Transactional(version = Version.WSAT10, enabled = false)
	public String getEngineProcessInstanceIdByBOId(String processDefinitionKey,
			String boId) {

		// Unique instance exists
		if (activitiAccessor == null) {
			getWorkflowAccessor();
		}
		RuntimeService runtimeService = activitiAccessor.getRuntimeService();

		ProcessInstance processInstance = runtimeService
				.createProcessInstanceQuery()
				.processInstanceBusinessKey(boId, processDefinitionKey)
				.singleResult();

		return processInstance == null ? null : processInstance
				.getProcessInstanceId();
	}

	@Override
	@Transactional(version = Version.WSAT10, enabled = false)
	public List<String[]> getTaskInstanceExtendAttrs(
			String engineTaskInstanceId) {
		
		// Delegate this operation
		Map<String, String> extendAttrMap = getWorkflowAccessor().getTaskInstanceExtendAttrs(engineTaskInstanceId);
		if (extendAttrMap == null) {
			return null;
		}
		List<String[]> forReturn = new ArrayList<String[]>(extendAttrMap.size());
		for (Entry<String, String> entry : extendAttrMap.entrySet()) {
			String[] keyValue = new String[] {entry.getKey(), entry.getValue()};
			forReturn.add(keyValue);
		}
		return forReturn;
	}

	@Override
	@Transactional(version = Version.WSAT10, enabled = false)
	public String getRootProcessInstanceId(String engineProcessInstanceId) throws ProcessException {
		
		// Unique instance exists
		if (activitiAccessor == null) {
			getWorkflowAccessor();
		}
		
		try {
			return activitiAccessor.obtainRootProcess(engineProcessInstanceId, true);
		} catch (Exception e) {
			throw new ProcessException("Maybe process" + engineProcessInstanceId + "is ended.", e);
		}
	}

}
