/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rill.bpm.ws;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.bpmn.deployer.BpmnDeployer;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.XMLImporter;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.engine.impl.util.ReflectUtil;
import org.junit.Assert;
import org.junit.Test;
import org.rill.bpm.ws.WSProcessEngineConfiguration;
import org.rill.bpm.ws.metro.WSImportToolImporterImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;


/**
 *
 * @author mengran
 */
@ContextConfiguration(value={"classpath*:applicationContext-*.xml"})
public class WSProcessEngineConfigurationTest extends AbstractJUnit4SpringContextTests {
    
    private static final Logger log = Logger.getLogger(WSProcessEngineConfigurationTest.class.getName());
    
    public WSProcessEngineConfigurationTest() {
    }
    
    @Resource
    private ProcessEngineConfiguration processEngineConfiguration;
    
    //@Deployment(resources = {"com/baidu/rigel/service/workflow/api/pg-support.bpmn20.xml"})
    @SuppressWarnings("unchecked")
    @Test
	public void testChangeWSXmlImporter() {
        
        for (Deployer deployer : ((ProcessEngineConfigurationImpl) processEngineConfiguration).getDeployers()) {
            // Find BpmnDeployer
            if (deployer instanceof BpmnDeployer) {
                Assert.assertTrue(((BpmnDeployer) deployer).getBpmnParser().getClass().equals(WSProcessEngineConfiguration.WSBpmnParser.class));
                BpmnParse bpmnParse = ((BpmnDeployer) deployer).getBpmnParser().createParse();
                Field importers = ReflectUtil.getField("importers", bpmnParse);
                Assert.assertNotNull(importers);
                try {
                    importers.setAccessible(true);
                    XMLImporter xmlImporter = ((Map<String, XMLImporter>) importers.get(bpmnParse)).get("http://schemas.xmlsoap.org/wsdl/");
                    Assert.assertTrue(xmlImporter.getClass().equals(WSImportToolImporterImpl.class));
                } catch (IllegalArgumentException ex) {
                    log.log(Level.SEVERE, "Exception when xmlImporter config", ex);
                    throw new ActivitiException("Exception when xmlImporter config", ex);
                } catch (IllegalAccessException ex) {
                    log.log(Level.SEVERE, "Exception when xmlImporter config", ex);
                    throw new ActivitiException("Exception when xmlImporter config", ex);
                }
            }
        }
    }
}
