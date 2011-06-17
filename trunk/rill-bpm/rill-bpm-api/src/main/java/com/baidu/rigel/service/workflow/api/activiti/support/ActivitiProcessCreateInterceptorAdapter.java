/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.api.activiti.support;

import com.baidu.rigel.service.workflow.api.ProcessCreateInteceptor;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;
import java.util.Map;
import java.util.logging.Logger;
import org.activiti.engine.runtime.ProcessInstance;

/**
 * Activiti engine process create interceptor adapter.
 * @author mengran
 */
public abstract class ActivitiProcessCreateInterceptorAdapter implements ProcessCreateInteceptor {

    /** Logger available to subclasses */
    protected final static Logger logger = Logger.getLogger(ActivitiProcessCreateInterceptorAdapter.class.getName());

    private void checkParam(Object modelInfo, Object processStarterInfo) {

        if (!(modelInfo instanceof String)) {
            throw new ProcessException("modelInfo must a String.");
        }

//        if (!(processStarterInfo instanceof String)) {
//            throw new ProcessException("processStarterInfo must a String.");
//        }
    }

    public final Object preOperation(Object modelInfo, Object processStarterInfo, Long businessObjectId, Map<String, Object> startParams) throws ProcessException {

        // Check context parameter
        checkParam(modelInfo, processStarterInfo);

        try {
            logger.fine("Execute process create interceptor#preOperation [" + this + "].");
            return doPreOperation((String) modelInfo, processStarterInfo, businessObjectId, startParams);
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }

    public final void postOperation(Object engineProcessInstance, Long businessObjectId, Object processStarterInfo) throws ProcessException {

        // Check context parameter
        if (!(engineProcessInstance instanceof ProcessInstance)) {
            throw new ProcessException("processInstance must a " + ProcessInstance.class.getName() + ".");
        }

        try {
            logger.fine("Execute process create interceptor#postOperation [" + this + "].");
            doPostOperation((ProcessInstance) engineProcessInstance, businessObjectId, processStarterInfo);
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }

    protected String doPreOperation(String modelInfo, Object processStarterInfo, Long businessObjectId, Map<String, Object> startParams) {

        return modelInfo;
    }

    protected void doPostOperation(ProcessInstance engineProcessInstance, Long businessObjectId, Object processStarterInfo) {

        // Do nothing
    }
}
