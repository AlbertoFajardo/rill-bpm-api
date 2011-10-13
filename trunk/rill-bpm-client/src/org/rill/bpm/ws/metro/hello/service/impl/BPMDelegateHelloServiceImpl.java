package org.rill.bpm.ws.metro.hello.service.impl;

import java.util.List;

import org.rill.bpm.ws.metro.hello.service.HelloService;
import org.springframework.transaction.annotation.Transactional;

import com.baidu.rigel.service.workflow.ws.client.CreateProcessInstanceDto;
import com.baidu.rigel.service.workflow.ws.client.MapElements;
import com.baidu.rigel.service.workflow.ws.client.MapElementsArray;
import com.baidu.rigel.service.workflow.ws.client.RemoteActivitiTemplateService;

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
        workflowAccessor.getRemoteActivitiTemplatePort().createProcessInstance(dto);
        
        // Get process instance ID
    	String engineProcessInstanceId = workflowAccessor.getRemoteActivitiTemplatePort().getEngineProcessInstanceIdByBOId(PROCESS_DEFINITION_KEY, businessObjectId);
    	System.out.println("Engine process instanceId:" + engineProcessInstanceId);
		
		// Finally do local logic
		getLocalDBHelloService().sayHello(name);
	}

	@Override
	public List<String> whoSaid() {

		// Finally do local logic
		return getLocalDBHelloService().whoSaid();
	}

}
