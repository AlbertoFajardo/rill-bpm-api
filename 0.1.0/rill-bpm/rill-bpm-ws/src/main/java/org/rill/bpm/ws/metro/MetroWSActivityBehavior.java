package org.rill.bpm.ws.metro;

import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.ws.metro.WSImportToolImporterImpl.DynamicClientDelegateWSOperation;


/**
 * Metro implementation of WS activity behavior.
 * 
 * @author mengran
 *
 */
public class MetroWSActivityBehavior extends AbstractBpmnActivityBehavior {

	protected final Log logger = LogFactory.getLog(getClass().getName());
	private DynamicClientDelegateWSOperation wsOperation;

	public MetroWSActivityBehavior(String operationRef, WSImportToolImporterImpl xmlImporter) {
		super();
		this.wsOperation = xmlImporter.getOperationByName(operationRef);
	}

	@Override
	public void execute(ActivityExecution execution) throws Exception {
		
		if (wsOperation != null) {
			// Invoke WS operation
			this.wsOperation.sendFor(this.wsOperation.generateInMessage(execution), null);
		} else {
			logger.warn("WS Operation is null. execution ID is " + execution.getProcessInstanceId());
		}
		
		// Do super's logic
		super.execute(execution);
	}
	
}
