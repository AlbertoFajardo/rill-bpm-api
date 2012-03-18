package org.rill.bpm.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.api.exception.ProcessException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.StringUtils;


public class ProcessCreateInterceptorAdapter implements ProcessCreateInteceptor, BeanFactoryAware {

	/** Logger available to subclasses */
    protected final Log logger = LogFactory.getLog(getClass().getName());
    
    protected AtomicReference<WorkflowOperations> workflowAccessor = new AtomicReference<WorkflowOperations>();
    private BeanFactory heldBeanFactory;
    @Resource(name="workflowCache")
	private WorkflowCache<HashMap<String, String>> workflowCache;
	
	public final WorkflowCache<HashMap<String, String>> getWorkflowCache() {
		return workflowCache;
	}

	public final void setWorkflowCache(WorkflowCache<HashMap<String, String>> workflowCache) {
		this.workflowCache = workflowCache;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		heldBeanFactory = beanFactory;
	}
    
    public final WorkflowOperations getWorkflowAccessor() {
		return workflowAccessor.get();
	}

	private void checkParam(String processDefinitionKey, String processStarter) {

        if (!StringUtils.hasText(processDefinitionKey)) {
            throw new ProcessException("processDefinitionKey must not empty.");
        }
        // Add process starter empty check at 2012-02-07
        if (!StringUtils.hasText(processStarter)) {
            throw new ProcessException("processStarter must not empty.");
        }
        
        // bean factory maybe null when jUnit
        if (workflowAccessor.get() == null) {
        	workflowAccessor.compareAndSet(null, heldBeanFactory.getBean("workflowAccessor", WorkflowOperations.class));
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

    	// Check context parameter
        checkParam(processDefinitionKey, processStarter);
        
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
