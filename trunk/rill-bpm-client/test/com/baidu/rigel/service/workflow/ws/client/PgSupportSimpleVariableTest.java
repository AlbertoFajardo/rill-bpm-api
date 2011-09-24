/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.ws.client;

import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Access pg-support process by WS
 * @author mengran
 */
public class PgSupportSimpleVariableTest {
    
    public PgSupportSimpleVariableTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
    public void testPgSupport() {
        
        String businessObjectId = "pg-support-simplevariable-" + new Random().nextInt();
        String processDefinitionKey = "pg-support-simplevariable";
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
        } catch (Exception e) {
        	Assert.fail(e.getMessage());
        }
        
    }
}
