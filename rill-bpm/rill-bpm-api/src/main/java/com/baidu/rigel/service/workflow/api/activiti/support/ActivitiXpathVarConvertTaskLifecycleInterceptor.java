/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.rigel.service.workflow.api.activiti.support;

import com.baidu.rigel.service.workflow.api.WorkflowOperations;
import com.baidu.rigel.service.workflow.api.activiti.ActivitiTaskExecutionContext;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;

/**
 * <b>
 *  Using expression "abc==empty string" is unrecommended.
 * 
 * @author mengran
 */
public class ActivitiXpathVarConvertTaskLifecycleInterceptor extends ActivitiTaskLifecycleInteceptorAdapter {

    private static final String ENGINE_VARIABLE_DEFINITION_PREFIX = "__";
    private static final String ENGINE_VARIABLE_DEFINITION_SPLIT = "_";
    private String engineVariableDefinitionSplit = ENGINE_VARIABLE_DEFINITION_SPLIT;
    private WorkflowOperations workflowAccessor;
    
    private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    {
        factory.setNamespaceAware(true); // never forget this!
    }

    public WorkflowOperations getWorkflowAccessor() {
        return workflowAccessor;
    }

    public void setWorkflowAccessor(WorkflowOperations workflowAccessor) {
        this.workflowAccessor = workflowAccessor;
    }

    /**
     * @return the engineVariableDefinitionSplit
     */
    public String getEngineVariableDefinitionSplit() {
        return engineVariableDefinitionSplit;
    }

    /**
     * @param engineVariableDefinitionSplit the engineVariableDefinitionSplit to set
     */
    public void setEngineVariableDefinitionSplit(
            String engineVariableDefinitionSplit) {
        this.engineVariableDefinitionSplit = engineVariableDefinitionSplit;
    }

    @Override
    protected void doPreComplete(ActivitiTaskExecutionContext taskExecutionContext) {

        // First get all process instance related variables
        Set<String> engineRelateDatanames = null;
        engineRelateDatanames = getWorkflowAccessor().getProcessInstanceVariableNames(taskExecutionContext.getProcessInstanceId());

        Map<String, Object> workflowParams = taskExecutionContext.getWorkflowParams();
        logger.log(Level.INFO, "Before Xpath variable convertion: {0}", ObjectUtils.getDisplayString(workflowParams));

        try {
            // Second use Xpath expression
            if (!CollectionUtils.isEmpty(engineRelateDatanames)) {
                for (String engineRelateDataname : engineRelateDatanames) {
                    if (!engineRelateDataname.startsWith(ENGINE_VARIABLE_DEFINITION_PREFIX)) {
                        // Default set work flow variables as false
                        if (!workflowParams.containsKey(engineRelateDataname)) {
                            logger.log(Level.FINE, "Put 0 for variable name:{0} into workflowParams", engineRelateDataname);
                            workflowParams.put(engineRelateDataname, "0");
                        }
                        continue;
                    }
                    
                    // Generate by Xpath
                    generateByXpath(workflowParams, engineRelateDataname);
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred when parse expression using XPath.", e);
            throw new ProcessException("Exception occurred when parse expression using XPath.", e);
        }
        
        logger.log(Level.INFO, "After Xpath variable convertion: {0}", ObjectUtils.getDisplayString(workflowParams));
    }

    protected void generateByXpath(Map<String, Object> workflowParams, String engineRelateDataname) {

        try {
            String expressionText = engineRelateDataname.substring(ENGINE_VARIABLE_DEFINITION_PREFIX.length());
            String[] split = StringUtils.delimitedListToStringArray(expressionText, engineVariableDefinitionSplit);
            String workflowParamValue = workflowParams.get(split[0]) == null ? "" : workflowParams.get(split[0]).toString();
            if (split.length > 1 && StringUtils.hasText(workflowParamValue)) {
                // Check it is XML or not
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new ByteArrayInputStream(workflowParamValue.getBytes("UTF-8")));
                XPathFactory xFactory = XPathFactory.newInstance();
                XPath xpath = xFactory.newXPath();
                StringBuilder sb = new StringBuilder();
                sb.append("//");
                for (int i = 1; i < split.length; i++) {
                    sb.append(split[i]);
                    sb.append("/");
                }
                sb.append("text()");
                logger.log(Level.FINE, "Build xPath:{0}", sb.toString());
                XPathExpression expr = xpath.compile(sb.toString());
                Object value = expr.evaluate(doc, XPathConstants.STRING);
                if (StringUtils.hasText(value.toString())) {
                    logger.log(Level.FINE, "Parse xPath:{0} and put value:{1} into workflowParams", new Object[]{sb.toString(), value});
                    workflowParams.put(engineRelateDataname, value);
                } else {
                    logger.log(Level.WARNING, "Can not get value using XPath because invalid engine data name:{0}", engineRelateDataname);
                }
            } else {
                logger.log(Level.WARNING, "Can not get value using XPath because invalid engine data name:{0}", engineRelateDataname);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception occurred when parse expression using Xpath. Do next one.", e);
        }
    }
    
}
