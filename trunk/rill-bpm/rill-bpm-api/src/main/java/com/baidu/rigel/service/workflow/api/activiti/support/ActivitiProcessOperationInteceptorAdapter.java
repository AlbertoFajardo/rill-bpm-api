package com.baidu.rigel.service.workflow.api.activiti.support;

import org.springframework.util.StringUtils;

import com.baidu.rigel.service.workflow.api.ProcessOperationInteceptor;
import com.baidu.rigel.service.workflow.api.WorkflowOperations;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;
import java.util.logging.Logger;

/**
 * Activiti engine process create interceptor adapter.
 * @author mengran
 **/
public abstract class ActivitiProcessOperationInteceptorAdapter implements
        ProcessOperationInteceptor {

    /** Logger available to subclasses */
    protected final static Logger logger = Logger.getLogger(ActivitiProcessOperationInteceptorAdapter.class.getName());

    public abstract WorkflowOperations.PROCESS_OPERATION_TYPE handleOpeationType();

    public void postOperation(String engineProcessInstanceId) throws ProcessException {

        try {
            logger.fine("Execute process operation interceptor#postOperation [" + this + "].");
            doPostOperation(engineProcessInstanceId);
        } catch (Exception e) {
            throw new ProcessException(e.getMessage());
        }
    }

    protected void doPostOperation(String engineProcessInstanceId) {
        // Do nothing
    }

    public final void preOperation(String engineProcessInstanceId,
            String operator, String reason) throws ProcessException {

        if (engineProcessInstanceId == null || !StringUtils.hasText(operator)) {
            throw new ProcessException("参数无效");
        }

        try {
            logger.fine("Execute process operation interceptor#preOperation [" + this + "].");
            doPreOperation(engineProcessInstanceId, operator, reason);
        } catch (Exception e) {
            throw new ProcessException(e.getMessage(), e);
        }
    }

    protected void doPreOperation(String engineProcessInstanceId,
            String operator, String reason) {
        // expose method to client
        // Do nothing
    }
}
