/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.baidu.rigel.service.workflow.api.activiti.support;

import com.baidu.rigel.service.workflow.api.WorkflowOperations;
import com.baidu.rigel.service.workflow.api.activiti.ActivitiTaskExecutionContext;
import org.springframework.util.Assert;

/**
 * Engine-driven design helper class.
 * @author mengran
 */
public abstract class EngineDrivenTLIAdapter<T> extends ActivitiTaskLifecycleInteceptorAdapter {

    @Override
    protected final void doPreComplete(ActivitiTaskExecutionContext taskExecutionContext) {

        T t = obtainTaskFormData(taskExecutionContext);
        Object returnObject = doEngineDriven(t, taskExecutionContext);
        if (returnObject != null) {
            taskExecutionContext.getWorkflowParams().put(WorkflowOperations.ENGINE_DRIVEN_TASK_RETURN_DATA_KEY, returnObject);
        }
    }

    protected final T obtainTaskFormData(ActivitiTaskExecutionContext taskExecutionContext) {

        Assert.notNull(taskExecutionContext);

        T t = (T) taskExecutionContext.getWorkflowParams().get(WorkflowOperations.ENGINE_DRIVEN_TASK_FORM_DATA_KEY);

        return t;
    }

    protected abstract Object doEngineDriven(T t, ActivitiTaskExecutionContext taskExecutionContext);

}
