/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
            logger.fine("Allow serialize variables, do nothing in [" + this.getClass().getName() + "].");
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
                        logger.fine("Parse difinition data:" + parseExpression);
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
