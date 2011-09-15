/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.ws;

import com.baidu.rigel.service.workflow.ws.metro.WSImportToolImporterImpl;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.deployer.BpmnDeployer;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.XMLImporter;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

/**
 *
 * @author mengran
 */
@ContextConfiguration(value={"classpath*:applicationContext-*.xml"})
public class WSProcessEngineConfigurationTest extends SpringActivitiTestCase {
    
    private static final Logger log = Logger.getLogger(WSProcessEngineConfigurationTest.class.getName());
    
    public WSProcessEngineConfigurationTest() {
    }
    
    //@Deployment(resources = {"com/baidu/rigel/service/workflow/api/pg-support.bpmn20.xml"})
    public void testChangeWSXmlImporter() {
        
        for (Deployer deployer : processEngineConfiguration.getDeployers()) {
            // Find BpmnDeployer
            if (deployer instanceof BpmnDeployer) {
                assertTrue(((BpmnDeployer) deployer).getBpmnParser().getClass().equals(WSProcessEngineConfiguration.WSBpmnParser.class));
                BpmnParse bpmnParse = ((BpmnDeployer) deployer).getBpmnParser().createParse();
                Field importers = ReflectUtil.getField("importers", bpmnParse);
                Assert.notNull(importers);
                try {
                    importers.setAccessible(true);
                    XMLImporter xmlImporter = ((Map<String, XMLImporter>) importers.get(bpmnParse)).get("http://schemas.xmlsoap.org/wsdl/");
                    Assert.isTrue(xmlImporter.getClass().equals(WSImportToolImporterImpl.class));
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
