package org.rill.bpm.ws.metro;

import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;


/**
 * Metro implementation of WS activity behavior.
 * 
 * @author mengran
 *
 */
public class MetroWSActivityBehavior extends AbstractBpmnActivityBehavior {

	private String operationRef;

	public MetroWSActivityBehavior(String operationRef) {
		super();
		this.operationRef = operationRef;
	}

	@Override
	public void execute(ActivityExecution execution) throws Exception {
		
		// Invoke WS operation
		
		
		// Do super's logic
		super.execute(execution);
	}
	
}
