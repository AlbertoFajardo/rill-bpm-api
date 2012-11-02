package org.rill.bpm.ws.lb;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.annotation.Resource;

import org.activiti.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.rill.bpm.PeerMethodTestHelperTaskExecutionListener;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.scaleout.ScaleoutKeySource;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(value="classpath:applicationContext-scaleout.xml")
@TestExecutionListeners({PeerMethodTestHelperTaskExecutionListener.class})
public class ScaleoutKeyTest extends AbstractJUnit4SpringContextTests {

	@Resource(name="workflowAccessor")
	private WorkflowOperations workflowAccessor;
	
	public void scaleoutAnnotations() {
		
		ScaleoutKeySource source = AnnotationUtils.findAnnotation(WorkflowOperations.class, ScaleoutKeySource.class);
		Assert.assertNotNull(source);
	}

	@Test
	@Deployment(resources = {
			"Scaleout.bpmn20.xml"
			 })
	public void createProcessInstance() {
		
		String boId = new Integer(new Random().nextInt()).toString();
		List<String> taskIds = workflowAccessor.createProcessInstance("Scaleout", "rillmeng", boId, null);
		
		taskIds = workflowAccessor.completeTaskInstance(taskIds.get(0), "Rill Meng", null);
		Assert.assertTrue(taskIds == null || taskIds.isEmpty());
		
		// Batch complete task instance
		String boId1 = new Integer(new Random().nextInt()).toString();
		List<String> taskIds1 = workflowAccessor.createProcessInstance("Scaleout", "rillmeng", boId1, null);
		Assert.assertTrue(taskIds1.size() == 1);
		
		String boId2 = new Integer(new Random().nextInt()).toString();
		List<String> taskIds2 = workflowAccessor.createProcessInstance("Scaleout", "rillmeng", boId2, null);
		Assert.assertTrue(taskIds2.size() == 1);
		
		Map<String, Map<String, Object>> batchDTO = new LinkedHashMap<String, Map<String,Object>>();
		batchDTO.put(taskIds1.get(0), null);
		batchDTO.put(taskIds2.get(0), null);
		Map<String, List<String>> batchResult = workflowAccessor.batchCompleteTaskIntances(batchDTO , "Batch");
		Assert.assertTrue(batchResult.size() == 2);
		for (Entry<String, List<String>> entry : batchResult.entrySet()) {
			Assert.assertTrue(entry == null || entry.getValue().isEmpty());
		}
	}

}
