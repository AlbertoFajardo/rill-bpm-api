package com.baidu.rigel.service.workflow.api;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.util.StringUtils;

import com.baidu.rigel.service.workflow.api.exception.ProcessException;

public class ProcessCreateInterceptorAdapter implements ProcessCreateInteceptor {

	/** Logger available to subclasses */
    protected final Logger logger = Logger.getLogger(getClass().getName());
    
    private void checkParam(String processDefinitionKey, String processStarter) {

        if (!StringUtils.hasText(processDefinitionKey)) {
            throw new ProcessException("processDefinitionKey must not empty.");
        }
    }
    
    public final void preOperation(String processDefinitionKey, String processStarter, String businessObjectId, Map<String, Object> startParams) throws ProcessException {

        // Check context parameter
        checkParam(processDefinitionKey, processStarter);

        try {
            logger.log(Level.FINE, "Execute process create interceptor#preOperation [{0}].", this);
            doPreOperation(processDefinitionKey, processStarter, businessObjectId, startParams);
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }

    public final void postOperation(String engineProcessInstanceId, String businessObjectId, String processStarter) throws ProcessException {

        try {
            logger.log(Level.FINE, "Execute process create interceptor#postOperation [{0}].", this);
            
            // FIXME: Need proxy it for prevent call some method.
            doPostOperation(engineProcessInstanceId, businessObjectId, processStarter);
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }
    
    protected String doPreOperation(String processDefinitionKey, String processStarter, String businessObjectId, Map<String, Object> startParams) {

        return processDefinitionKey;
    }

    protected void doPostOperation(String engineProcessInstanceId, String businessObjectId, String processStarter) {

        // Do nothing
    }

}
