/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.ws.client;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.rill.bpm.ws.metro.hello.service.HelloService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * Access pg-support process by WS.
 * <p>
 * 	NO NOT RUN THIS TESTCASE IN MULTIPLE-THREAD, IT'S NOT THREAD-SAFE.
 * 
 * @author mengran
 */
@ContextConfiguration(value={"classpath:/conf/applicationContext-*.xml"})
public class PgSupportSimpleVariableTest extends AbstractTransactionalJUnit4SpringContextTests {
    
	private HelloService helloService;
	
    public final HelloService getHelloService() {
		return helloService;
	}

	public final void setHelloService(HelloService helloService) {
		this.helloService = helloService;
	}

	public PgSupportSimpleVariableTest() {
    }
	
	protected static String preBusinessObjectId;

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
//    @Rollback(value=false)
    public void rollBackWSAT() {
        
        String businessObjectId = "pg-support-simplevariable-" + new Random().nextInt();
        String processDefinitionKey = "pg-support-simplevariable";
        // Back to static variable
        preBusinessObjectId = businessObjectId;
        
        RemoteActivitiTemplateService workflowAccessor = new RemoteActivitiTemplateService();
        
        // Fill DTO
        CreateProcessInstanceDto dto = new CreateProcessInstanceDto();
        dto.setBusinessObjectId(businessObjectId);
        dto.setProcessDefinitionKey(processDefinitionKey);
        dto.setProcessStarter("ws.client");
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
        try {
        	String engineProcessInstanceId = workflowAccessor.getRemoteActivitiTemplatePort().getEngineProcessInstanceIdByBOId(processDefinitionKey, businessObjectId);
        	System.out.println("Engine process instanceId:" + engineProcessInstanceId);
        	getHelloService().sayHello("engineProcessInstanceId:" + engineProcessInstanceId);
        } catch (Exception e) {
        	Assert.fail(e.getMessage());
        }
        
    }
    
    @Test
    public void verifyWSAT() {
    	
    	// Use static variable
    	String businessObjectId = preBusinessObjectId;
        String processDefinitionKey = "pg-support-simplevariable";
        
        RemoteActivitiTemplateService workflowAccessor = new RemoteActivitiTemplateService();
        
    	// Get process instance ID
        try {
        	String engineProcessInstanceId = workflowAccessor.getRemoteActivitiTemplatePort().getEngineProcessInstanceIdByBOId(processDefinitionKey, businessObjectId);
        	System.out.println("Engine process instanceId:" + engineProcessInstanceId);
        	Assert.assertNull("WSAT-Rollback feature is not effected.", engineProcessInstanceId);
        } catch (Exception e) {
        	Assert.fail(e.getMessage());
        }
    }
}
