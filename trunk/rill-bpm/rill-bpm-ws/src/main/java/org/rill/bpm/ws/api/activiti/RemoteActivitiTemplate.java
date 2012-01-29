/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rill.bpm.ws.api.activiti;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.activiti.ActivitiAccessor;
import org.rill.bpm.api.exception.ProcessException;
import org.rill.bpm.ws.api.RemoteWorkflowOperations;
import org.springframework.util.Assert;
import org.springframework.web.context.support.WebApplicationContextUtils;

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

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass().getName());
    
	// Must use getter method!
	private WorkflowOperations workflowAccessor;
	private RobustActivitiTemplate activitiTemplate;
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

			activitiTemplate = ActivitiAccessor.retrieveActivitiAccessorImpl(workflowAccessor, RobustActivitiTemplate.class);
		}

		Assert.notNull(workflowAccessor);
		return workflowAccessor;
	}

	@Transactional(version = Version.WSAT10)
	public RemoteWorkflowResponse createProcessInstance(
			CreateProcessInstanceDto createProcessInstanceDto)
			throws ProcessException {

		Map<String, Object> passToEngine = new HashMap<String, Object>();
		if (createProcessInstanceDto.getStartParams() != null) {
			for(Entry<String, String> entry : createProcessInstanceDto.getStartParams().entrySet()) {
				passToEngine.put(entry.getKey(), entry.getValue());
			}
		}
		// Delegate this operations
		final List<String> tasks = getWorkflowAccessor().createProcessInstance(
				createProcessInstanceDto.getProcessDefinitionKey(),
				createProcessInstanceDto.getProcessStarter(),
				createProcessInstanceDto.getBusinessObjectId(),
				passToEngine);

		final String engineProcessInstanceId = activitiTemplate.getEngineProcessInstanceIdByBOId(
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

		final Map<String, Object> passToEngine = new HashMap<String, Object>();
		if (completeTaskInstanceDto.getWorkflowParams() != null) {
			for(Entry<String, String> entry : completeTaskInstanceDto.getWorkflowParams().entrySet()) {
				passToEngine.put(entry.getKey(), entry.getValue());
			}
		}
		
		return activitiTemplate
				.runExtraCommand(new Command<RemoteWorkflowResponse>() {

					@Override
					public RemoteWorkflowResponse execute(
							CommandContext commandContext) {

						TaskEntity taskEntity = commandContext
								.getTaskManager()
								.findTaskById(completeTaskInstanceDto.getEngineTaskInstanceId());
						// Handle retrieve
						if (taskEntity == null) {
							logger.warn("Can not find task instance by id " + completeTaskInstanceDto.getEngineTaskInstanceId() + ", maybe it has beed completed.");
							return activitiTemplate.handleTaskInstanceHasEnd(completeTaskInstanceDto.getEngineTaskInstanceId(), commandContext);
						}
						
						String engineProcessInstanceId = taskEntity.getProcessInstanceId();
						// Cache root process instance ID
						String rootProcessInstanceId = activitiTemplate.obtainRootProcess(engineProcessInstanceId, true);
						ExecutionEntity ee = commandContext
								.getExecutionManager().findExecutionById(
										engineProcessInstanceId);
						ProcessDefinitionEntity pde = commandContext
								.getProcessDefinitionManager()
								.findLatestProcessDefinitionById(
										ee.getProcessDefinitionId());
						String processDefinitionKey = pde.getKey();
						
						ExecutionEntity rootee = commandContext
								.getExecutionManager().findExecutionById(
										rootProcessInstanceId);
						String businessKey = rootee.getBusinessKey();
						
						// Delegate this operations
						final List<String> tasks = getWorkflowAccessor().completeTaskInstance(
								completeTaskInstanceDto.getEngineTaskInstanceId(),
								completeTaskInstanceDto.getOperator(),
								passToEngine);
						
						ProcessInstance processInstance = activitiTemplate.getRuntimeService().createProcessInstanceQuery().processInstanceId(engineProcessInstanceId).singleResult();
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
		if (activitiTemplate == null) {
			getWorkflowAccessor();
		}
		RuntimeService runtimeService = activitiTemplate.getRuntimeService();
		// Do delete operation
		runtimeService.deleteProcessInstance(engineProcessInstanceId, reason);
	}
	
	// ----------------------------------------- Read API as below ----------//

//	@Transactional(version = Version.WSAT10, enabled = false)
	public String getEngineProcessInstanceIdByBOId(String processDefinitionKey,
			String boId) {

		// Unique instance exists
		if (activitiTemplate == null) {
			getWorkflowAccessor();
		}
		RuntimeService runtimeService = activitiTemplate.getRuntimeService();

		ProcessInstance processInstance = runtimeService
				.createProcessInstanceQuery()
				.processInstanceBusinessKey(boId, processDefinitionKey)
				.singleResult();

		return processInstance == null ? null : processInstance
				.getProcessInstanceId();
	}

	@Override
//	@Transactional(version = Version.WSAT10, enabled = false)
	public List<String[]> getTaskInstanceExtendAttrs(
			String engineTaskInstanceId) {
		
		// Delegate this operation
		Map<String, String> extendAttrMap = getWorkflowAccessor().getTaskInstanceInformations(engineTaskInstanceId);
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
//	@Transactional(version = Version.WSAT10, enabled = false)
	public String getRootProcessInstanceId(String engineProcessInstanceId) throws ProcessException {
		
		// Unique instance exists
		if (activitiTemplate == null) {
			getWorkflowAccessor();
		}
		
		try {
			return activitiTemplate.obtainRootProcess(engineProcessInstanceId, true);
		} catch (Exception e) {
			throw new ProcessException("Maybe process" + engineProcessInstanceId + "is ended.", e);
		}
	}

	@Override
//	@Transactional(version = Version.WSAT10, enabled = false)
	public String[] getProcessInstanceVariableNames(
			String engineProcessInstanceId) {
		
		// Unique instance exists
		if (activitiTemplate == null) {
			getWorkflowAccessor();
		}
		
		try {
			Set<String> processInstanceNames = activitiTemplate.getProcessInstanceVariableNames(engineProcessInstanceId);
			return processInstanceNames == null ? null : processInstanceNames.toArray(new String[processInstanceNames.size()]); 
		} catch (Exception e) {
			throw new ProcessException("Maybe process" + engineProcessInstanceId + "is ended.", e);
		}
	}

	@Override
//	@Transactional(version = Version.WSAT10, enabled = false)
	public String[] getLastedVersionProcessDefinitionVariableNames(
			String processDefinitionKey) {
		
		// Unique instance exists
		if (activitiTemplate == null) {
			getWorkflowAccessor();
		}
		
		try {
			Set<String> processInstanceNames = activitiTemplate.getLastedVersionProcessDefinitionVariableNames(processDefinitionKey);
			return processInstanceNames == null ? null : processInstanceNames.toArray(new String[processInstanceNames.size()]); 
		} catch (Exception e) {
			throw new ProcessException("Can not get variables by process definition key " + processDefinitionKey, e);
		}
				
	}

}
