package org.rill.bpm.ws.metro.hello.service.impl;

import java.util.List;

import org.rill.bpm.ws.metro.hello.service.HelloService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.baidu.rigel.service.workflow.ws.client.CreateProcessInstanceDto;
import com.baidu.rigel.service.workflow.ws.client.MapElements;
import com.baidu.rigel.service.workflow.ws.client.MapElementsArray;
import com.baidu.rigel.service.workflow.ws.client.RemoteActivitiTemplateService;
import com.baidu.rigel.service.workflow.ws.client.RemoteWorkflowResponse;

public class BPMDelegateHelloServiceImpl implements HelloService {

	public static final String PROCESS_DEFINITION_KEY = "pg-support-simplevariable";
	
	private HelloService localDBHelloService;
	
	public final HelloService getLocalDBHelloService() {
		return localDBHelloService;
	}

	public final void setLocalDBHelloService(HelloService localDBHelloService) {
		this.localDBHelloService = localDBHelloService;
	}

	@Override
	@Transactional
	public void sayHello(String name) {

		String businessObjectId = "pg-support-simplevariable-" + name;
        RemoteActivitiTemplateService workflowAccessor = new RemoteActivitiTemplateService();
        
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
        
        // Start a process instance
        RemoteWorkflowResponse response = workflowAccessor.getRemoteActivitiTemplatePort().createProcessInstance(dto);
        
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
