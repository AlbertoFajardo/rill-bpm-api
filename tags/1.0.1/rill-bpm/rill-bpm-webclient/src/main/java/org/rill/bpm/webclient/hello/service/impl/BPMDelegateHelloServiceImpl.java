package org.rill.bpm.webclient.hello.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.webclient.hello.service.HelloService;
import org.rill.bpm.ws.client.CompleteTaskInstanceDto;
import org.rill.bpm.ws.client.CreateProcessInstanceDto;
import org.rill.bpm.ws.client.MapElements;
import org.rill.bpm.ws.client.MapElementsArray;
import org.rill.bpm.ws.client.RemoteActivitiTemplate;
import org.rill.bpm.ws.client.RemoteWorkflowResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

public class BPMDelegateHelloServiceImpl implements HelloService {

	protected final Log logger = LogFactory.getLog(getClass().getName());
	
	public static final String PROCESS_DEFINITION_KEY = "Sp-ms-ws";
	
	private ConcurrentHashMap<String, String> taskIdProcessIdMap = new ConcurrentHashMap<String, String>();
	
	private HelloService localDBHelloService;
	
	public final HelloService getLocalDBHelloService() {
		return localDBHelloService;
	}

	public final void setLocalDBHelloService(HelloService localDBHelloService) {
		this.localDBHelloService = localDBHelloService;
	}

	@Resource(name="remoteActivitiTemplate")
	private RemoteActivitiTemplate remoteActivitiTemplate;

	@Override
	@Transactional
	public void sayHello(String name, int cnt) {
		
		Assert.isTrue(cnt > 1, "Invalid say hello cnt.");
		for (int i = 0; i < cnt; i++) {
			this.sayHello(name + "_" + i);
		}
	}
	
	@Override
	@Transactional
	public void sayHello(String name) {

		String businessObjectId = PROCESS_DEFINITION_KEY + "_" + name;
        
        // Fill DTO
        CreateProcessInstanceDto dto = new CreateProcessInstanceDto();
        dto.setBusinessObjectId(businessObjectId);
        dto.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);
        dto.setProcessStarter(name.split("_")[0]);
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
        RemoteWorkflowResponse response = remoteActivitiTemplate.createProcessInstance(dto);
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
    	String engineProcessInstanceId = remoteActivitiTemplate.getEngineProcessInstanceIdByBOId(PROCESS_DEFINITION_KEY, businessObjectId);
    	Assert.isTrue(engineProcessInstanceId == null, "Activiti has commited? WS-AT does not work. [engineProcessInstanceId= " + engineProcessInstanceId + "]" + sb.toString());
    	Assert.isTrue(response.getBusinessObjectId().equals(businessObjectId));
    	Assert.isTrue(response.getProcessDefinitionKey().equals(PROCESS_DEFINITION_KEY));
    	Assert.isTrue(response.getEngineTaskInstanceIds().size() == 1);
		
		// Finally do local logic
		getLocalDBHelloService().sayHello(name + " " + response.getEngineProcessInstanceId());
		
		// Put into map.
		taskIdProcessIdMap.put(response.getEngineTaskInstanceIds().get(0), response.getRootEngineProcessInstanceId());
	}

	@Override
	public List<String> whoSaid() {

		// Finally do local logic
		return getLocalDBHelloService().whoSaid();
	}

	@Override
	@Transactional
	public void batchSayHello(String[] names) {
		
		Assert.notEmpty(names);
		
		List<CompleteTaskInstanceDto> list = new ArrayList<CompleteTaskInstanceDto>(names.length);
		for (String name : names) {
			Entry<String, String> entry = taskIdProcessIdMap.entrySet().iterator().next();
			CompleteTaskInstanceDto dto = new CompleteTaskInstanceDto();
			dto.setEngineTaskInstanceId(entry.getKey());
			dto.setOperator("batchSayHello");
			MapElementsArray mea = new MapElementsArray();
			MapElements me = new MapElements();
			me.setKey("__audit_action");
			me.setValue("1");
			mea.getItem().add(me);
			dto.setWorkflowParams(mea);
			list.add(dto);
			logger.info("Batch sayHello: " + name);
			// Remove from map
			String taskId = taskIdProcessIdMap.remove(entry.getKey());
			Assert.notNull(taskId, "Concurrent try to complete this." + entry.getKey() + " " + entry.getValue());
		}
		
		List<RemoteWorkflowResponse> response = new ArrayList<RemoteWorkflowResponse>();
		if (names.length == 2 && names[0].equals(names[1])) {
			for (int i = 0; i < 2; i++) {
				logger.info(names[i] + " " + i);
				response.add(remoteActivitiTemplate.completeTaskInstance(list.get(i)));
			}
		} else {
			response = remoteActivitiTemplate.batchCompleteTaskInstance(list);
		}
		
		for (RemoteWorkflowResponse res : response) {
			logger.info("Batch sayHello: " + res.getBusinessObjectId());
			logger.info("Batch sayHello: " + ObjectUtils.getDisplayString(res.getEngineTaskInstanceIds()));
		}
	}

	@Override
	@Transactional
	public void deleteSayHello(String name) {
		
		List<String> whoSaid = whoSaid();
		for (String who : whoSaid) {
			if (who.startsWith(name)) {
				logger.info("Delete sayHello process: " + who);
				remoteActivitiTemplate.deleteProcessInstance(who.split(" ")[1], name);
				localDBHelloService.deleteSayHello(who);
				break;
			}
		}
	}

}
