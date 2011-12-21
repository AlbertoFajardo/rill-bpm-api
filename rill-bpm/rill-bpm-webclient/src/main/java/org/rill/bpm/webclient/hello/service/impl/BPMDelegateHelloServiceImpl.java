package org.rill.bpm.webclient.hello.service.impl;

import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;

import org.rill.bpm.webclient.hello.service.HelloService;
import org.rill.bpm.ws.client.CreateProcessInstanceDto;
import org.rill.bpm.ws.client.MapElements;
import org.rill.bpm.ws.client.MapElementsArray;
import org.rill.bpm.ws.client.RemoteActivitiTemplateService;
import org.rill.bpm.ws.client.RemoteWorkflowResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

public class BPMDelegateHelloServiceImpl implements HelloService {

	private final Logger logger = Logger.getLogger(getClass().getName());
	
	public static final String PROCESS_DEFINITION_KEY = "Sp-ms";
	
	private HelloService localDBHelloService;
	
	public final HelloService getLocalDBHelloService() {
		return localDBHelloService;
	}

	public final void setLocalDBHelloService(HelloService localDBHelloService) {
		this.localDBHelloService = localDBHelloService;
	}

	@Resource(name="remoteActivitiTemplateService")
	private RemoteActivitiTemplateService remoteActivitiTemplateService;

	@Override
	@Transactional
	public void sayHello(String name) {

		String businessObjectId = PROCESS_DEFINITION_KEY + "_" + name;
        RemoteActivitiTemplateService workflowAccessor = remoteActivitiTemplateService;
        
        // Fill DTO
        CreateProcessInstanceDto dto = new CreateProcessInstanceDto();
        dto.setBusinessObjectId(businessObjectId);
        dto.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);
        dto.setProcessStarter(name);
        MapElementsArray mea = new MapElementsArray();
        dto.setStartParams(mea);
        
        // Fill start parameters
        MapElements me = new MapElements();
        me.setKey("startKeys");
        me.setValue("Hello BPMServer");
        mea.getItem().add(me);
        MapElements level = new MapElements();
        me.setKey("level");
        me.setValue("A");
        mea.getItem().add(level);
       
        
        logger.info("Start process[" + PROCESS_DEFINITION_KEY + "]" + " by " + name + " with businessObjectId " + businessObjectId);
        // Start a process instance
        RemoteWorkflowResponse response = workflowAccessor.getRemoteActivitiTemplatePort().createProcessInstance(dto);
        StringBuilder sb = new StringBuilder();
        sb.append("getEngineProcessInstanceId " + response.getEngineProcessInstanceId());
        sb.append("getEngineTaskInstanceIds " + ObjectUtils.getDisplayString(response.getEngineTaskInstanceIds()));
        sb.append("getProcessDefinitionKey " + response.getProcessDefinitionKey());
        sb.append("getBusinessObjectId " + response.getBusinessObjectId());
        sb.append("getRootEngineProcessInstanceId " + response.getRootEngineProcessInstanceId());
        sb.append("isHasParentProcessInstance " + response.isHasParentProcessInstance());
        sb.append("isProcessInstanceEnd " + response.isProcessInstanceEnd());
        sb.append("isRobustReturn " + response.isRobustReturn());
        logger.info("Start process result: " + sb.toString());
        
        // Get process instance ID
    	String engineProcessInstanceId = workflowAccessor.getRemoteActivitiTemplatePort().getEngineProcessInstanceIdByBOId(PROCESS_DEFINITION_KEY, businessObjectId);
    	Assert.isTrue(engineProcessInstanceId == null, "Activiti has commited? WS-AT does not work.");
    	Assert.isTrue(response.getBusinessObjectId().equals(businessObjectId));
    	Assert.isTrue(response.getProcessDefinitionKey().equals(PROCESS_DEFINITION_KEY));
    	Assert.isTrue(response.getEngineTaskInstanceIds().size() == 1);
		
		// Finally do local logic
		getLocalDBHelloService().sayHello(name + " " + response.getEngineProcessInstanceId());
	}

	@Override
	public List<String> whoSaid() {

		// Finally do local logic
		return getLocalDBHelloService().whoSaid();
	}

}
