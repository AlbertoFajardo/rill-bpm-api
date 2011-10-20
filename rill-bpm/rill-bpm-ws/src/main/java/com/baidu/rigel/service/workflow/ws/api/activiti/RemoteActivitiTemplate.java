/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.ws.api.activiti;

import java.util.List;
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
 * 	Use @com.sun.xml.ws.api.tx.at.Transactional to mark some WS method is under WS-AT specification.
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
             workflowAccessor = WebApplicationContextUtils.getRequiredWebApplicationContext(
                (ServletContext) context.getMessageContext().get(MessageContext.SERVLET_CONTEXT)).getBean("workflowAccessor", WorkflowOperations.class);
             
             if (workflowAccessor instanceof SpringProxy) {
         		Object targetSource;
     			try {
     				targetSource = ((Advised) workflowAccessor).getTargetSource().getTarget();
     				while (targetSource instanceof SpringProxy) {
     	    			targetSource = ((Advised) targetSource).getTargetSource().getTarget();
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
    
    @Transactional(version=Version.WSAT10)
    public RemoteWorkflowResponse createProcessInstance(CreateProcessInstanceDto createProcessInstanceDto) throws ProcessException {
        
        // Delegate this operations
        final List<String> tasks = getWorkflowAccessor().createProcessInstance(createProcessInstanceDto.getProcessDefinitionKey(), 
                createProcessInstanceDto.getProcessStarter(), createProcessInstanceDto.getBusinessObjectId(), 
                createProcessInstanceDto.getStartParams());
        
        String engineProcessInstanceId = activitiAccessor.runExtraCommand(new Command<String>() {

			@Override
			public String execute(CommandContext commandContext) {
				
				String taskId = tasks.size() > 0 ? tasks.get(0) : null;
				if (taskId == null) {
					return null;
				} else {
					return commandContext.getTaskManager().findTaskById(taskId).getProcessInstanceId();
				}
			}
        	
        });
        
        return new RemoteWorkflowResponse(engineProcessInstanceId, createProcessInstanceDto.getBusinessObjectId(), 
        		createProcessInstanceDto.getProcessDefinitionKey(), tasks);
    }
    
    @Transactional(version=Version.WSAT10)
    public RemoteWorkflowResponse completeTaskInstance(final CompleteTaskInstanceDto completeTaskInstanceDto) throws ProcessException {
        
        // Delegate this operations
    	final List<String> tasks = getWorkflowAccessor().completeTaskInstance(completeTaskInstanceDto.getEngineTaskInstanceId(), 
                completeTaskInstanceDto.getOperator(), completeTaskInstanceDto.getWorkflowParams());
    	
    	return activitiAccessor.runExtraCommand(new Command<RemoteWorkflowResponse>() {

			@Override
			public RemoteWorkflowResponse execute(CommandContext commandContext) {
				
				String engineProcessInstanceId = commandContext.getTaskManager().
						findTaskById(completeTaskInstanceDto.getEngineTaskInstanceId()).getProcessInstanceId();
				ExecutionEntity ee = commandContext.getExecutionManager().findExecutionById(engineProcessInstanceId);
				ProcessDefinitionEntity pde = commandContext.getProcessDefinitionManager().
						findLatestProcessDefinitionById(ee.getProcessDefinitionId());
				return new RemoteWorkflowResponse(engineProcessInstanceId, ee.getBusinessKey(), 
						pde.getKey(), tasks);
			}
        	
        });
    	
    }

    @Transactional(version=Version.WSAT10, enabled=false)
    public String getEngineProcessInstanceIdByBOId(String processDefinitionKey, String boId) {
        
        // Unique instance exists 
    	if (activitiAccessor == null) {
    		getWorkflowAccessor();
    	}
    	RuntimeService runtimeService = activitiAccessor.getRuntimeService();
    	
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(boId, processDefinitionKey)
                .singleResult();
        
        return processInstance == null ? null : processInstance.getProcessInstanceId();
    }
    
}
