/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.ws.api.activiti;

import com.baidu.rigel.service.workflow.api.WorkflowOperations;
import com.baidu.rigel.service.workflow.api.activiti.ActivitiAccessor;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;
import com.baidu.rigel.service.workflow.ws.api.RemoteWorkflowOperations;
import com.sun.xml.ws.api.tx.at.Transactional;
import com.sun.xml.ws.api.tx.at.Transactional.Version;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.util.Assert;
import org.springframework.web.context.support.WebApplicationContextUtils;

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
    private AtomicBoolean needRetrieve = new AtomicBoolean(true);
    
    @Resource
    private WebServiceContext context;

    private WorkflowOperations getWorkflowAccessor() {
        if (needRetrieve.compareAndSet(true, false)) {
             workflowAccessor = WebApplicationContextUtils.getRequiredWebApplicationContext(
                (ServletContext) context.getMessageContext().get(MessageContext.SERVLET_CONTEXT)).getBean("workflowAccessor", WorkflowOperations.class);
        }
        Assert.notNull(workflowAccessor);
        return workflowAccessor;
    }
    
    @Transactional(version=Version.WSAT10)
    public void createProcessInstance(CreateProcessInstanceDto createProcessInstanceDto) throws ProcessException {
        
        // Delegate this operations
        getWorkflowAccessor().createProcessInstance(createProcessInstanceDto.getProcessDefinitionKey(), 
                createProcessInstanceDto.getProcessStarter(), createProcessInstanceDto.getBusinessObjectId(), 
                createProcessInstanceDto.getStartParams());
    }
    
    @Transactional(version=Version.WSAT10)
    public void completeTaskInstance(CompleteTaskInstanceDto completeTaskInstanceDto) throws ProcessException {
        
        // Delegate this operations
        getWorkflowAccessor().completeTaskInstance(completeTaskInstanceDto.getEngineTaskInstanceId(), 
                completeTaskInstanceDto.getOperator(), completeTaskInstanceDto.getWorkflowParams());
    }

    // ------------------------------- NOT PUBLISH YET -----------------------//
    @WebMethod(exclude=true)
    public void createProcessInstance(String processDefinitionKey, String processStarter, String businessObjectId, Map<String, String> startParams) throws ProcessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @WebMethod(exclude=true)
    public void terminalProcessInstance(String engineProcessInstanceId, String operator, String reason) throws ProcessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @WebMethod(exclude=true)
    public void suspendProcessInstance(String engineProcessInstanceId, String operator, String reason) throws ProcessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @WebMethod(exclude=true)
    public void resumeProcessInstance(String engineProcessInstanceId, String operator, String reason) throws ProcessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @WebMethod(exclude=true)
    public void completeTaskInstance(String engineTaskInstanceId, String operator, Map<String, String> workflowParams) throws ProcessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @WebMethod(exclude=true)
    public void batchCompleteTaskIntances(Map<String, Map<String, String>> batchDTO, String operator) throws ProcessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @WebMethod(exclude=true)
    public HashMap<String, String> getTaskInstanceExtendAttrs(String engineTaskInstanceId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @WebMethod(exclude=true)
    public String getTaskNameByDefineId(String processDefinitionKey, String taskDefineId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @WebMethod(exclude=true)
    public Set<String> getProcessInstanceVariableNames(String engineProcessInstanceId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @WebMethod(exclude=true)
    public void abortTaskInstance(String engineTaskInstanceId) throws ProcessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @WebMethod(exclude=true)
    public String obtainTaskRole(String engineTaskInstanceId) throws ProcessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @WebMethod(exclude=true)
    public void reassignTaskExecuter(String engineProcessInstanceId, String engineTaskInstanceId, String oldExecuter, String newExecuter) throws ProcessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Transactional(version=Version.WSAT10, enabled=false)
    public String getEngineProcessInstanceIdByBOId(String processDefinitionKey, String boId) {
        
        // Unique instance exists 
    	RuntimeService runtimeService = null;
    	if (getWorkflowAccessor() instanceof SpringProxy) {
    		Object targetSource;
			try {
				targetSource = ((Advised) getWorkflowAccessor()).getTargetSource().getTarget();
				while (targetSource instanceof SpringProxy) {
	    			targetSource = ((Advised) targetSource).getTargetSource().getTarget();
	    		}
			} catch (Exception e) {
				throw new ProcessException(e);
			}
    		
    		runtimeService = ((ActivitiAccessor) (targetSource)).getRuntimeService();
    	} else {
    		runtimeService = ((ActivitiAccessor) getWorkflowAccessor()).getRuntimeService();
    	}
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(boId, processDefinitionKey)
                .singleResult();
        
        return processInstance == null ? null : processInstance.getProcessInstanceId();
    }
    
}
