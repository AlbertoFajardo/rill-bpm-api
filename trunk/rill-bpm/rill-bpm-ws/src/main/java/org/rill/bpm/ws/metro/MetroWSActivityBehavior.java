package org.rill.bpm.ws.metro;

import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.rill.bpm.ws.metro.WSImportToolImporterImpl.DynamicClientDelegateWSOperation;
import org.springframework.util.Assert;


/**
 * Metro implementation of WS activity behavior.
 * 
 * @author mengran
 *
 */
public class MetroWSActivityBehavior extends AbstractBpmnActivityBehavior {

	private DynamicClientDelegateWSOperation wsOperation;

	public MetroWSActivityBehavior(String operationRef, WSImportToolImporterImpl xmlImporter) {
		super();
		this.wsOperation = xmlImporter.getOperationByName(operationRef);
		Assert.notNull(wsOperation, "Can not find ws-operation by " + operationRef);
	}

	@Override
	public void execute(ActivityExecution execution) throws Exception {
		
		// Invoke WS operation
		this.wsOperation.sendFor(this.wsOperation.generateInMessage(execution), null);
		
		// Do super's logic
		super.execute(execution);
	}
	
}
