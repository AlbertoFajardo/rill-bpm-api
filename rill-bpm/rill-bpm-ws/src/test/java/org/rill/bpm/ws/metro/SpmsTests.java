package org.rill.bpm.ws.metro;

import java.util.List;
import java.util.Random;

import org.activiti.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.rill.bpm.PeerMethodTestHelperTaskExecutionListener;
import org.rill.bpm.api.WorkflowOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(value="classpath:activiti.cfg.xml")
@TestExecutionListeners({PeerMethodTestHelperTaskExecutionListener.class})
public class SpmsTests extends AbstractJUnit4SpringContextTests {

	@Autowired
	private WorkflowOperations workflowAccessor;
	
	@Deployment(resources = {
			"sp-ms.bpmn20.xml"
			 })
	@Test
	public void spmsTest() {
		
		List<String> taskIds = workflowAccessor.createProcessInstance("Sp-ms-ws", "ws-test", new Integer(new Random().nextInt()).toString(), null);
		Assert.assertEquals(1, taskIds.size());
		
		try {
			Thread.sleep(11120000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
