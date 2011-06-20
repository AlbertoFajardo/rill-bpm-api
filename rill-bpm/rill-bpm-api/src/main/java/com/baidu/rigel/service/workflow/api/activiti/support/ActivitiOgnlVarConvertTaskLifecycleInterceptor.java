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
import com.baidu.rigel.service.workflow.api.activiti.ActivitiAccessor;
import com.baidu.rigel.service.workflow.api.activiti.ActivitiTaskExecutionContext;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.apache.ibatis.ognl.Ognl;
import org.apache.ibatis.ognl.OgnlContext;
import org.apache.ibatis.ognl.OgnlException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 *
 * @author mengran
 */
public class ActivitiOgnlVarConvertTaskLifecycleInterceptor extends ActivitiTaskLifecycleInteceptorAdapter {

    private static final String ENGINE_VARIABLE_DEFINITION_PREFIX = "__";
    private static final String ENGINE_VARIABLE_DEFINITION_SPLIT = "_";
    private String engineVariableDefinitionSplit = ENGINE_VARIABLE_DEFINITION_SPLIT;
    private WorkflowOperations workflowAccessor;

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

        // Allow serialize variables, do nothing.
        if (((ActivitiAccessor) getWorkflowAccessor()).isSerializeVarPermission()) {
            logger.log(Level.FINE, "Allow serialize variables, do nothing in [{0}].", this.getClass().getName());
            return;
        }
        // First get all process instance related variables
        Set<String> engineRelateDatanames = null;
        engineRelateDatanames = getWorkflowAccessor().getProcessInstanceVariableNames(taskExecutionContext.getProcessInstanceId());

        Map<String, Object> workflowParams = taskExecutionContext.getWorkflowParams();

        try {
            // Second use ONGL expression
            if (!CollectionUtils.isEmpty(engineRelateDatanames)) {
                // Prepare OGNL
                OgnlContext context = new OgnlContext();
                context.putAll(workflowParams);

                for (String engineRelateDataname : engineRelateDatanames) {
                    if (!engineRelateDataname.startsWith(ENGINE_VARIABLE_DEFINITION_PREFIX)) {
                        // Default set work flow variables as Flase
                        if (!workflowParams.containsKey(engineRelateDataname)) {
                            workflowParams.put(engineRelateDataname, "0");
                        }
                        continue;
                    }

                    try {
                        Object parseExpression = Ognl.parseExpression(convertEngineDataName(engineRelateDataname.substring(ENGINE_VARIABLE_DEFINITION_PREFIX.length())));
                        logger.log(Level.FINE, "Parse difinition data:{0}", parseExpression);
                        Object value = Ognl.getValue(parseExpression, context);
                        if (value != null) {
                            workflowParams.put(engineRelateDataname, value);
                        }
                    } catch (OgnlException e) {
                        logger.log(Level.WARNING, "Exception occurred when parse expression using OGNL. Do next one.", e);
                    }
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred when parse expression using OGNL.", e);
            throw new ProcessException("Exception occurred when parse expression using OGNL.", e);
        }

    }

    protected String convertEngineDataName(String engineRelateDataname) {

        if (StringUtils.hasText(engineRelateDataname)) {

            return engineRelateDataname.trim().replaceAll(getEngineVariableDefinitionSplit(), ".");
        }

        throw new ProcessException("Process variable name is empty???");
    }
}
