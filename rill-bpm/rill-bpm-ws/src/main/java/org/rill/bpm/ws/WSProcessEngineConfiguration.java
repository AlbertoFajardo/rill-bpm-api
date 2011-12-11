/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rill.bpm.ws;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.bpmn.deployer.BpmnDeployer;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.bpmn.parser.XMLImporter;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.engine.impl.util.ReflectUtil;
import org.rill.bpm.api.activiti.RillProcessEngineConfiguration;
import org.springframework.util.Assert;


/**
 * WS extension process engine configuration.
 * 
 * @author mengran
 */
public class WSProcessEngineConfiguration extends
		RillProcessEngineConfiguration {

	private XMLImporter wsXmlImporter;

	public XMLImporter getWsXmlImporter() {
		return wsXmlImporter;
	}

	public void setWsXmlImporter(XMLImporter wsXmlImporter) {
		this.wsXmlImporter = wsXmlImporter;
	}

	@Override
	public ProcessEngine buildProcessEngine() {
		ProcessEngine processEngine = super.buildProcessEngine();
		
		// Change WS importer
		for (Deployer deployer : getDeployers()) {
			// Find BpmnDeployer
			if (deployer instanceof BpmnDeployer) {
				((BpmnDeployer) deployer).setBpmnParser(new WSBpmnParser(
						expressionManager));
				log.log(Level.INFO, "Change BpmnParser implementation to {0}",
						WSBpmnParser.class.getName());
			}
		}

		return processEngine;
	}

	public class WSBpmnParser extends BpmnParser {

		public WSBpmnParser(ExpressionManager expressionManager) {
			super(expressionManager);
		}

		@SuppressWarnings("unchecked")
		@Override
		public BpmnParse createParse() {
			BpmnParse bpmnParse = super.createParse();

			// Change WS importer implementation
			Field importers = ReflectUtil.getField("importers", bpmnParse);
			Assert.notNull(importers);
			try {
				importers.setAccessible(true);
				((Map<String, XMLImporter>) importers.get(bpmnParse)).put(
						"http://schemas.xmlsoap.org/wsdl/", getWsXmlImporter());
			} catch (IllegalArgumentException ex) {
				log.log(Level.SEVERE, "Exception when xmlImporter config", ex);
				throw new ActivitiException(
						"Exception when xmlImporter config", ex);
			} catch (IllegalAccessException ex) {
				log.log(Level.SEVERE, "Exception when xmlImporter config", ex);
				throw new ActivitiException(
						"Exception when xmlImporter config", ex);
			}

			return bpmnParse;
		}

	}
	


}
