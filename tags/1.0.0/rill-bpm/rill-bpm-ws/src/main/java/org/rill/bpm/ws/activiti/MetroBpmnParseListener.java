package org.rill.bpm.ws.activiti;

import java.util.List;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.parser.BpmnParseListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.el.FixedValue;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.variable.VariableDeclaration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.ws.metro.MetroWSActivityBehavior;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Parse WS service task configurations.
 * 
 * @author mengran
 *
 */
public class MetroBpmnParseListener implements BpmnParseListener, BeanFactoryAware {

	public static final String METRO_WEB_SERVICE = "MetroWebService";
	
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
	protected final Log logger = LogFactory.getLog(getClass().getName());
	public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope,
			ActivityImpl activity) {
		
		String expression = serviceTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "expression");
		// Handle METRO_WEB_SERVICE configuration
		if (expression != null && expression.trim().length() > 0 && expression.equals(METRO_WEB_SERVICE)) {
			
			WSProcessEngineConfiguration configuration = internalBeanFactory.getBean(WSProcessEngineConfiguration.class);
			List<FieldDeclaration> fieldDeclarations = configuration.getBpmnParser().createParse().parseFieldDeclarations(serviceTaskElement);
			String location = null, operationQName = null, xsdIndex = null, username=null, password=null, portQName=null;
			for (FieldDeclaration fd : fieldDeclarations) {
				String fieldValue = fd.getType().equals(Expression.class.getName()) ? 
						((FixedValue) fd.getValue()).getExpressionText() : fd.getValue().toString();
				if (fd.getName().equals("location")) {
					location = fieldValue;
				}
				if (fd.getName().equals("portQName")) {
					portQName = fieldValue;
				}
				if (fd.getName().equals("operationQName")) {
					operationQName = fieldValue;
				}
				if (fd.getName().equals("xsdIndex")) {
					xsdIndex = fieldValue;
				}
				// Add HTTP BASIC AUTH support
				if (fd.getName().equals("username")) {
					username = fieldValue;
				}
				if (fd.getName().equals("password")) {
					password = fieldValue;
				}
			}
			// Add null check at 2012-02-07
			Assert.notNull(location, METRO_WEB_SERVICE + " must configure field: location");
			Assert.notNull(portQName, METRO_WEB_SERVICE + " must configure field: portQName");
			Assert.notNull(operationQName, METRO_WEB_SERVICE + " must configure field: operationQName");
			
			try {
				configuration.getWsXmlImporter().importFrom(location, null, portQName, username, password);
				configuration.getWsXmlImporter().importSchema(location, xsdIndex);
			} catch (Exception e) {
				StringBuilder sb = new StringBuilder();
				sb.append("location: " + ObjectUtils.getDisplayString(location));
				sb.append(" operationQName: " + ObjectUtils.getDisplayString(operationQName));
				sb.append(" xsdIndex: " + ObjectUtils.getDisplayString(xsdIndex));
				logger.warn("Exception occurred when try to import metro web service." + sb.toString(), e);
			}
			
			String operationRef = operationQName;
			MetroWSActivityBehavior webServiceActivityBehavior = new MetroWSActivityBehavior(operationRef, 
					configuration.getWsXmlImporter());
			activity.setActivityBehavior(webServiceActivityBehavior);
		}
	}

	private BeanFactory internalBeanFactory;
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
	
		this.internalBeanFactory = beanFactory;
	}

	
}
