package org.rill.bpm.ws.metro;

import org.activiti.engine.impl.bpmn.parser.BpmnParseListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.variable.VariableDeclaration;

/**
 * Parse WS service task configurations.
 * 
 * @author mengran
 *
 */
public class MetroBpmnParseListener implements BpmnParseListener {

	public void parseProcess(Element processElement,
			ProcessDefinitionEntity processDefinition) {
		// Do nothing
	}

	public void parseStartEvent(Element startEventElement, ScopeImpl scope,
			ActivityImpl startEventActivity) {
		// Do nothing
	}

	public void parseExclusiveGateway(Element exclusiveGwElement,
			ScopeImpl scope, ActivityImpl activity) {
		// Do nothing
	}

	public void parseParallelGateway(Element parallelGwElement,
			ScopeImpl scope, ActivityImpl activity) {
		// Do nothing
	}

	public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope,
			ActivityImpl activity) {
		// Do nothing
	}

	public void parseBusinessRuleTask(Element businessRuleTaskElement,
			ScopeImpl scope, ActivityImpl activity) {
		// Do nothing
	}

	public void parseTask(Element taskElement, ScopeImpl scope,
			ActivityImpl activity) {
		// Do nothing
	}

	public void parseManualTask(Element manualTaskElement, ScopeImpl scope,
			ActivityImpl activity) {
		// Do nothing
	}

	public void parseUserTask(Element userTaskElement, ScopeImpl scope,
			ActivityImpl activity) {
		// Do nothing
	}

	public void parseEndEvent(Element endEventElement, ScopeImpl scope,
			ActivityImpl activity) {
		// Do nothing
	}

	public void parseBoundaryTimerEventDefinition(Element timerEventDefinition,
			boolean interrupting, ActivityImpl timerActivity) {
		// Do nothing
	}

	public void parseBoundaryErrorEventDefinition(Element errorEventDefinition,
			boolean interrupting, ActivityImpl activity,
			ActivityImpl nestedErrorEventActivity) {
		// Do nothing
	}

	public void parseSubProcess(Element subProcessElement, ScopeImpl scope,
			ActivityImpl activity) {
		// Do nothing
	}

	public void parseCallActivity(Element callActivityElement, ScopeImpl scope,
			ActivityImpl activity) {
		// Do nothing
	}

	public void parseProperty(Element propertyElement,
			VariableDeclaration variableDeclaration, ActivityImpl activity) {
		// Do nothing
	}

	public void parseSequenceFlow(Element sequenceFlowElement,
			ScopeImpl scopeElement, TransitionImpl transition) {
		// Do nothing
	}

	public void parseSendTask(Element sendTaskElement, ScopeImpl scope,
			ActivityImpl activity) {
		// Do nothing
	}

	public void parseMultiInstanceLoopCharacteristics(Element activityElement,
			Element multiInstanceLoopCharacteristicsElement,
			ActivityImpl activity) {
		// Do nothing
	}

	public void parseIntermediateTimerEventDefinition(
			Element timerEventDefinition, ActivityImpl timerActivity) {
		// Do nothing
	}
	
	// ------------------------ Implementation ------------------------------//
	public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope,
			ActivityImpl activity) {
		
		final String ws = "##MetroWebService#";
		
		String expression = serviceTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "expression");
		if (expression != null && expression.trim().length() > 0 && expression.startsWith(ws)) {
			String operationRef = expression.substring(ws.length());
			MetroWSActivityBehavior webServiceActivityBehavior = new MetroWSActivityBehavior(operationRef);
			activity.setActivityBehavior(webServiceActivityBehavior);
		}
	}

	
}
