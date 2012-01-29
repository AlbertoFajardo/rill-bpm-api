package org.rill.bpm.api;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.api.exception.ProcessException;
import org.springframework.util.StringUtils;


public class ProcessCreateInterceptorAdapter implements ProcessCreateInteceptor {

	/** Logger available to subclasses */
    protected final Log logger = LogFactory.getLog(getClass().getName());
    
    private void checkParam(String processDefinitionKey, String processStarter) {

        if (!StringUtils.hasText(processDefinitionKey)) {
            throw new ProcessException("processDefinitionKey must not empty.");
        }
    }
    
    public final void preOperation(String processDefinitionKey, String processStarter, String businessObjectId, Map<String, Object> startParams) throws ProcessException {

        // Check context parameter
        checkParam(processDefinitionKey, processStarter);

        try {
            logger.debug("Execute process create interceptor#preOperation" + this.getClass().getName() + ".");
            doPreOperation(processDefinitionKey, processStarter, businessObjectId, startParams);
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }

    public final void postOperation(String processDefinitionKey, String engineProcessInstanceId, String businessObjectId, String processStarter) throws ProcessException {

        try {
            logger.debug("Execute process create interceptor#postOperation" + this.getClass().getName() + ".");
            
            // FIXME: Need proxy it for prevent call some method.
            doPostOperation(processDefinitionKey, engineProcessInstanceId, businessObjectId, processStarter);
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }
    
    protected String doPreOperation(String processDefinitionKey, String processStarter, String businessObjectId, Map<String, Object> startParams) {

        return processDefinitionKey;
    }

    protected void doPostOperation(String processDefinitionKey, String engineProcessInstanceId, String businessObjectId, String processStarter) {

        // Do nothing
    }

}
