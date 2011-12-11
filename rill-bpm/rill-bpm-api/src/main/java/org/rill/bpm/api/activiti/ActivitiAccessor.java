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
package org.rill.bpm.api.activiti;

import java.util.List;
import java.util.logging.Level;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.ServiceImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.rill.bpm.api.ProcessOperationInteceptor;
import org.rill.bpm.api.ThreadLocalResourceHolder;
import org.rill.bpm.api.WorkflowCache;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.WorkflowTemplate;
import org.rill.bpm.api.exception.ProcessException;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;


/**
 * Activiti engine access helper class
 * 
 * @author mengran
 */
public abstract class ActivitiAccessor extends WorkflowTemplate implements InitializingBean, BeanFactoryAware, ApplicationEventPublisherAware {

	public static final String ENGINE_BUILDING_TRANSACTION_PROPAGATION_EXPOSE = ActivitiAccessor.class.getName() + ".ENGINE_BUILDING_TRANSACTION_PROPAGATION_EXPOSE";
	
    private RuntimeService runtimeService;
    private TaskService taskService;
    private RepositoryService repositoryService;
    private IdentityService identityService;
    private FormService formService;
    private HistoryService historyService;
    private List<ProcessOperationInteceptor> processOperationInteceptors;
    private ActivitiExtraService extraService;
    private ProcessEngine processEngine;
    private ProcessEngineConfiguration processEngineConfiguration;
    
    private WorkflowCache workflowCache;

    public final WorkflowCache getCache() {
		return workflowCache;
	}

	public final void setCache(WorkflowCache cache) {
		this.workflowCache = cache;
	}
	
	public final HistoryService getHistoryService() {
		return historyService;
	}

	public final void setHistoryService(HistoryService historyService) {
		this.historyService = historyService;
	}

	public FormService getFormService() {
        return formService;
    }

    public void setFormService(FormService formService) {
        this.formService = formService;
    }

    public ProcessEngineConfiguration getProcessEngineConfiguration() {
        return processEngineConfiguration;
    }

    public void setProcessEngineConfiguration(ProcessEngineConfiguration processEngineConfiguration) {
        this.processEngineConfiguration = processEngineConfiguration;
    }

    public ProcessEngine getProcessEngine() {
        return processEngine;
    }

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    public RepositoryService getRepositoryService() {
        return repositoryService;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public IdentityService getIdentityService() {
        return identityService;
    }

    public void setIdentityService(IdentityService identityService) {
        this.identityService = identityService;
    }

    /**
     * @return the runtimeService
     */
    public final RuntimeService getRuntimeService() {
        return runtimeService;
    }

    /**
     * @param runtimeService the runtimeService to set
     */
    public final void setRuntimeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    /**
     * @return the processOperationInteceptors
     */
    public List<ProcessOperationInteceptor> getProcessOperationInteceptors() {
        return processOperationInteceptors;
    }

    /**
     * @param processOperationInteceptors the processOperationInteceptors to set
     */
    public void setProcessOperationInteceptors(
            List<ProcessOperationInteceptor> processOperationInteceptors) {
        this.processOperationInteceptors = processOperationInteceptors;
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Package access allowed.
     * @return Activiti extra service for run Command
     */
    ActivitiExtraService getExtraService() {
        return extraService;
    }
    
    @SuppressWarnings("unchecked")
	public static <T> T retrieveActivitiAccessorImpl(WorkflowOperations workflowAccessor, Class<T> clazz) {
    	
    	if (workflowAccessor instanceof SpringProxy) {
			Object targetSource;
			try {
				targetSource = ((Advised) workflowAccessor)
						.getTargetSource().getTarget();
				while (targetSource instanceof SpringProxy) {
					targetSource = ((Advised) targetSource)
							.getTargetSource().getTarget();
				}
			} catch (Exception e) {
				throw new ProcessException(e);
			}

			return (T) targetSource;
		} else {
			return (T) workflowAccessor;
		}
    	
    }

    /**
     * (SPI method and for internal usage)
     * @param <T> type of return
     * @param command command to execute
     * @return execute result
     */
    public final <T> T runExtraCommand(Command<T> command) {

        return getExtraService().doOperation(command);
    }

    public void afterPropertiesSet() throws Exception {
    	
    	// Do super's logic first.
    	super.afterPropertiesSet();
    	
        if (this.getProcessEngine() == null) {
            Assert.notNull(this.getProcessEngineConfiguration(), "Properties 'ProcessEngineConfiguration' is required.");
            
            // We specify transaction propagation for fix MySQL's SQLException: 
            // XAER_RMFAIL: The command cannot be executed when global transaction is in the  ACTIVE state
            ThreadLocalResourceHolder.bindProperty(ENGINE_BUILDING_TRANSACTION_PROPAGATION_EXPOSE, new Integer(TransactionDefinition.PROPAGATION_NOT_SUPPORTED));
			try {
            	ActivitiAccessor.this.setProcessEngine(getProcessEngineConfiguration().buildProcessEngine());
			} finally {
				ThreadLocalResourceHolder.unbindProperty(ENGINE_BUILDING_TRANSACTION_PROPAGATION_EXPOSE);
			}
            logger.log(Level.INFO, "Build process engine from it''s configuration.{0}", getProcessEngine());
        } else {
            logger.log(Level.INFO, "Retrieve process engine from inject property.{0}", getProcessEngine());
        }

        if (this.getProcessEngineConfiguration() == null) {
            Assert.notNull(this.getProcessEngine(), "Properties 'processEngine' is required.");
            this.setProcessEngineConfiguration(((ProcessEngineImpl) getProcessEngine()).getProcessEngineConfiguration());
            logger.log(Level.INFO, "Retrieve process configuration from processEngine.{0}", getProcessEngine());
        }

        // Retrieve service from engine if not inject with property
        if (getRuntimeService() == null) {
            this.setRuntimeService(getProcessEngine().getRuntimeService());
        }
        if (getTaskService() == null) {
            this.setTaskService(getProcessEngine().getTaskService());
        }
        if (getRepositoryService() == null) {
            this.setRepositoryService(getProcessEngine().getRepositoryService());
        }
        if (getIdentityService() == null) {
            this.setIdentityService(getProcessEngine().getIdentityService());
        }
        if (getFormService() == null) {
            this.setFormService(getProcessEngine().getFormService());
        }
        if (getHistoryService() == null) {
        	this.setHistoryService(getProcessEngine().getHistoryService());
        }

        // Initialize extra service
        this.extraService = new ActivitiExtraService();
        BeanUtils.copyProperties(this.getRuntimeService(), this.extraService);

        // Handle cache service
        if (workflowCache == null) {
        	workflowCache = getBeanFactory().getBean("workflowCache", WorkflowCache.class);
        }
        Assert.notNull(workflowCache, "Can not find " + WorkflowCache.class + " implementation in application context.");

    }

    private class ActivitiExtraService extends ServiceImpl {

        public <T> T doOperation(Command<T> command) {

            logger.log(Level.INFO, "Run extra command[{0}].", command);
            return getCommandExecutor().execute(command);
        }
    }

    /**
     * Obtain cache informations from cache. Cache data will fill in every node in cluster.
     *
     * <p>
     *  For work fine, we first find from external cache. Fall back to original cache(JVM cache) if any exception occurred.
     * 
     * @param taskInstanceId task instance's ID
     * @return cache information specify by enum given
     */
    protected String obtainCacheInfos(String taskInstanceId, TaskInformations cacheInfo) {

        Assert.hasText(taskInstanceId, "taskInstanceId pass in must not empty");
        Assert.notNull(cacheInfo, "taskInformations must not null");

        String cacheHit = null;
        logger.log(Level.FINE, "get Task related informations of taskInstanceId: " + taskInstanceId + ", key is " + cacheInfo);
        cacheHit = workflowCache.getTaskRelatedInfo(taskInstanceId, cacheInfo);

        return cacheHit;
    }

    public final String obtainRootProcess(String processInstanceId, boolean includeCallActivity) {
    	
    	String rootProcessNotCrossCallActivity = findRootProcessNotCrossCallActivity(processInstanceId);
        ExecutionEntity pi = (ExecutionEntity) getRuntimeService().createProcessInstanceQuery().processInstanceId(rootProcessNotCrossCallActivity).singleResult();
        ExecutionEntity rootEE = pi;
        while (rootEE.getSuperExecutionId() != null) {
        	ExecutionEntity superEE = (ExecutionEntity) getRuntimeService().createExecutionQuery().executionId(pi.getSuperExecutionId()).singleResult();
        	logger.log(Level.FINER, "Found super execution entity{0}, maybe this task is in callActivity scope.", new Object[]{superEE.getId()});
        	rootEE = superEE;
        	rootProcessNotCrossCallActivity = obtainRootProcess(superEE.getProcessInstanceId(), true);
        }
        logger.log(Level.FINE, "Return root execution entity{0}", new Object[]{rootEE.getId()});
        
        return rootProcessNotCrossCallActivity;
    }
    
    public final String obtainRootProcess(String processInstanceId) {
    	
    	// Delegate this operation
    	return obtainRootProcess(processInstanceId, false);
    }
    
    private String findRootProcessNotCrossCallActivity(String processInstanceId) {

        Assert.hasText(processInstanceId);

        String parentProcessId = processInstanceId;
        String rootProcessId = processInstanceId;
        while (parentProcessId != null) {
            try {
                rootProcessId = parentProcessId;
                ProcessInstance pi = getRuntimeService().createProcessInstanceQuery().subProcessInstanceId(parentProcessId).singleResult();
                logger.log(Level.FINEST, "Found parent process instance [{0}] of [{1}", new Object[]{ObjectUtils.getDisplayString(pi), parentProcessId});
                parentProcessId = pi == null ? null : pi.getProcessInstanceId();
            } catch (ActivitiException e) {
                throw new ProcessException("Can not found local process instance when try to handle sub-process.", e);
            }
        }

        logger.log(Level.FINE, "Return root process ID[{0}] of {1}", new Object[]{rootProcessId, processInstanceId});
        return rootProcessId;
    }

}
