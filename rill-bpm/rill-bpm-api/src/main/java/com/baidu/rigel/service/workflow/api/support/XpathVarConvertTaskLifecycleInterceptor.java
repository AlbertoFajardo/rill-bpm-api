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
package com.baidu.rigel.service.workflow.api.support;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;

import com.baidu.rigel.service.workflow.api.TaskExecutionContext;
import com.baidu.rigel.service.workflow.api.TaskLifecycleInteceptorAdapter;
import com.baidu.rigel.service.workflow.api.WorkflowOperations;
import com.baidu.rigel.service.workflow.api.WorkflowOperations.XStreamSerializeHelper;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;

/**
 * <b>
 *  Using expression "abc==empty string" is un-recommended.
 * 
 * @author mengran
 */
public class XpathVarConvertTaskLifecycleInterceptor extends TaskLifecycleInteceptorAdapter {

    public static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    static {
    	factory.setNamespaceAware(true); // never forget this!
    }
    
    protected static final Logger log = Logger.getLogger(XpathVarConvertTaskLifecycleInterceptor.class.getName());
    
    private static final String ENGINE_VARIABLE_DEFINITION_PREFIX = "__";
    private static final String ENGINE_VARIABLE_DEFINITION_SPLIT = "_";
//    private String engineVariableDefinitionSplit = ENGINE_VARIABLE_DEFINITION_SPLIT;
    private WorkflowOperations workflowAccessor;

    public WorkflowOperations getWorkflowAccessor() {
        return workflowAccessor;
    }

    public void setWorkflowAccessor(WorkflowOperations workflowAccessor) {
        this.workflowAccessor = workflowAccessor;
    }

//    /**
//     * @return the engineVariableDefinitionSplit
//     */
//    public String getEngineVariableDefinitionSplit() {
//        return engineVariableDefinitionSplit;
//    }
//
//    /**
//     * @param engineVariableDefinitionSplit the engineVariableDefinitionSplit to set
//     */
//    public void setEngineVariableDefinitionSplit(
//            String engineVariableDefinitionSplit) {
//        this.engineVariableDefinitionSplit = engineVariableDefinitionSplit;
//    }

    @Override
    protected void doPreComplete(TaskExecutionContext taskExecutionContext) {

        // First get all process instance related variables
        Set<String> engineRelateDatanames = null;
        engineRelateDatanames = getWorkflowAccessor().getProcessInstanceVariableNames(taskExecutionContext.getProcessInstanceId());

        Map<String, Object> convertAndFilter = convertAndFilter(engineRelateDatanames, taskExecutionContext.getWorkflowParams());
        
        log.log(Level.INFO, "Change workflow parameter map to : {0}", ObjectUtils.getDisplayString(convertAndFilter));
        taskExecutionContext.getWorkflowParams().putAll(convertAndFilter);
    }
    
    /**
     * @param engineRelateDatanames
     * @param workflowParams
     * @return after convert and filter. NOT NULL
     */
    public static Map<String, Object> convertAndFilter(Set<String> engineRelateDatanames, Map<String, Object> workflowParams) {
    	
        log.log(Level.INFO, "Before Xpath variable convertion: {0}", ObjectUtils.getDisplayString(workflowParams));
        
        Map<String, Object> convertAndFilter = new HashMap<String, Object>();
        try {
            // Second use Xpath expression
            if (!CollectionUtils.isEmpty(engineRelateDatanames)) {
                for (String engineRelateDataname : engineRelateDatanames) {
                	if (!workflowParams.containsKey(engineRelateDataname)) {
                    	// FIXME: Do not set unrelated variables to 0
//                        logger.log(Level.FINE, "Put 0 for variable name:{0} into workflowParams", engineRelateDataname);
//                        workflowParams.put(engineRelateDataname, "0");
                		if (!engineRelateDataname.startsWith(ENGINE_VARIABLE_DEFINITION_PREFIX)) {
                			log.log(Level.FINE, "Ignore unrelated variables${0} and do not change it's value", engineRelateDataname);
                		} else {
                			// Generate by Xpath
                            generateByXpath(workflowParams, engineRelateDataname, convertAndFilter);
                		}
                    } else {
                    	convertAndFilter.put(engineRelateDataname, workflowParams.get(engineRelateDataname));
                    }
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Exception occurred when parse expression using XPath.", e);
            throw new ProcessException("Exception occurred when parse expression using XPath.", e);
        }
        
        log.log(Level.INFO, "After Xpath variable convertion: {0}", ObjectUtils.getDisplayString(workflowParams));
        return convertAndFilter;
    }

    protected static void generateByXpath(Map<String, Object> workflowParams, String engineRelateDataname, Map<String, Object> convertAndFilter) {

    	String expressionText = engineRelateDataname.substring(ENGINE_VARIABLE_DEFINITION_PREFIX.length());
        String[] split = StringUtils.delimitedListToStringArray(expressionText, ENGINE_VARIABLE_DEFINITION_SPLIT);
        if (split.length > 1 && workflowParams.containsKey(split[0]) && workflowParams.get(split[0]) != null 
        		&& ClassUtils.isPrimitiveOrWrapper(workflowParams.get(split[0]).getClass())) {
        	throw new ProcessException("Can not generate engine variable " + engineRelateDataname + " from workflow param" + workflowParams.get(split[0]));
        }
        
        try {
            if (split.length > 1 && workflowParams.containsKey(split[0]) && workflowParams.get(split[0]) != null) {
            	String workflowParamValue = (workflowParams.get(split[0]) instanceof String) ? workflowParams.get(split[0]).toString() 
            			: XStreamSerializeHelper.serializeXml(split[0], workflowParams.get(split[0]));
            	log.log(Level.FINE, "After XStream serialize :{0}", workflowParamValue);
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
                log.log(Level.FINE, "Build xPath:{0}", sb.toString());
                XPathExpression expr = xpath.compile(sb.toString());
                String value = (String) expr.evaluate(doc, XPathConstants.STRING);
                if (StringUtils.hasText(value.toString())) {
                    log.log(Level.FINE, "Parse xPath:{0} and save value:{1}", new Object[]{sb.toString(), value});
                    convertAndFilter.put(engineRelateDataname, value);
                } else {
                    log.log(Level.WARNING, "Can not get value using XPath because invalid engine data name:{0}", engineRelateDataname);
                }
            } else {
                log.log(Level.WARNING, "Can not get value using XPath because invalid engine data name:{0}", engineRelateDataname);
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Exception occurred when parse expression using Xpath. Do next one.", e);
        }
        
    }
    
}
