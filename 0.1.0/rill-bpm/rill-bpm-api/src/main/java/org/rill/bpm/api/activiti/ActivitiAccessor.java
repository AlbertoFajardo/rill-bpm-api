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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.ServiceImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.rill.bpm.api.ProcessOperationInteceptor;
import org.rill.bpm.api.ThreadLocalResourceHolder;
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
import org.springframework.util.StringUtils;


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
				
				if (targetSource == null && ((Advised) workflowAccessor).getAdvisors().length > 0) {
					// Maybe load balance proxy
					throw new UnsupportedOperationException();
				}
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
    	
    	try {
    		return getExtraService().doOperation(command);
    	} catch (Exception e) {
    		logger.error("Exception occurred when runExtraCommand.", e);
    		throw new ProcessException(e);
    	}
    }

    @Override
	public String getName() {
		return getProcessEngine().getName();
	}

	@Override
	public int hashCode() {
		return getProcessEngine().getName().hashCode();
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
            logger.info("Build process engine from it''s configuration." + getProcessEngine());
        } else {
            logger.info("Retrieve process engine from inject property." + getProcessEngine());
        }

        if (this.getProcessEngineConfiguration() == null) {
            Assert.notNull(this.getProcessEngine(), "Properties 'processEngine' is required.");
            this.setProcessEngineConfiguration(((ProcessEngineImpl) getProcessEngine()).getProcessEngineConfiguration());
            logger.info("Retrieve process configuration from processEngine." + getProcessEngine());
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

    }

    private class ActivitiExtraService extends ServiceImpl {

        public <T> T doOperation(Command<T> command) {

            logger.info("Run extra command " + command.getClass().getName());
            return getCommandExecutor().execute(command);
        }
    }
    
	@Override
	public HashMap<String, String> getProcessInstanceInformations(
			final String engineProcessInstanceId) {
		
		try {
            HashMap<String, String> extendAttrsMap = new HashMap<String, String>();
            
            // Put informations into extend attributes map
            for (ProcessInformations key : ProcessInformations.values()) {
            	ProcessInstance rootProcessInstance = null;
                String rootProcessInstanceId = null;
                try {
                	rootProcessInstanceId = obtainRootProcess(engineProcessInstanceId, true);
                	rootProcessInstance = (ProcessInstance) getRuntimeService().createProcessInstanceQuery().processInstanceId(rootProcessInstanceId).singleResult();
                } catch (Exception e) {
                    throw new ProcessException("Can't get process instance by giving ID" + engineProcessInstanceId, e);
                }
                if (WorkflowOperations.ProcessInformations.P_ROOT_PROCESS_INSTANCE_ID.equals(key)) {
                	logger.debug("NOT HIT: method[getProcessRelatedInfo] cache key:" + engineProcessInstanceId + "," + key + "; value:" + rootProcessInstanceId);
                	extendAttrsMap.put(ProcessInformations.P_ROOT_PROCESS_INSTANCE_ID.name(), rootProcessInstanceId);
                }
                if (WorkflowOperations.ProcessInformations.P_BUSINESS_OBJECT_ID.equals(key)) {
                	logger.debug("NOT HIT: method[getProcessRelatedInfo] cache key:" + engineProcessInstanceId + "," + key + "; value:" + rootProcessInstance.getBusinessKey());
                	extendAttrsMap.put(ProcessInformations.P_BUSINESS_OBJECT_ID.name(), rootProcessInstance.getBusinessKey());
                }
                if (WorkflowOperations.ProcessInformations.P_PROCESS_DEFINE_KEY.equals(key)) {
                	String processDefinitionKey = runExtraCommand(new Command<String>() {

        				@Override
        				public String execute(CommandContext commandContext) {
        					ExecutionEntity ee = commandContext.getExecutionManager().findExecutionById(engineProcessInstanceId);
        					ProcessDefinitionEntity pde = commandContext.getProcessDefinitionManager().findLatestProcessDefinitionById(ee.getProcessDefinitionId());
        					return pde.getKey();
        				}
                		
                    });
                	logger.debug("NOT HIT: method[getProcessRelatedInfo] cache key:" + engineProcessInstanceId + "," + key + "; value:" + processDefinitionKey);
                	extendAttrsMap.put(ProcessInformations.P_PROCESS_DEFINE_KEY.name(), processDefinitionKey);
                }
            }
            return extendAttrsMap;
        } catch (Exception e) {
            throw new ProcessException("Can not obtain process[" + engineProcessInstanceId + "] extension attribute", e);
        }
	}

	/* (non-Javadoc)
	 * @see org.rill.bpm.api.WorkflowOperations#getTaskInstanceInformations(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, String> getTaskInstanceInformations(String taskInstanceId) {

        try {
            Map<String, String> extendAttrsMap = new HashMap<String, String>();
            
            // Put informations into extend attributes map
            for (TaskInformations ti : TaskInformations.values()) {
            	extendAttrsMap.put(ti.name(), getTaskRelatedInfo(taskInstanceId, ti));
            }
            String extendAttrs = extendAttrsMap.get(TaskInformations.EXTEND_ATTRIBUTES.name());
            if (StringUtils.hasText(extendAttrs)) {
	            Map<String, String> deserializeMap = XStreamSerializeHelper.deserializeObject(extendAttrs, "extendAttrs", Map.class);
	            extendAttrsMap.putAll(deserializeMap);
            }
            
            HashMap<String, String> forReturn = new HashMap<String, String>();

            forReturn.putAll(extendAttrsMap);
            logger.debug("PARSING EXTEND ATTRS--Task[" + taskInstanceId + "] description/Extend attributes holder result:" + ObjectUtils.getDisplayString(forReturn));
            return forReturn;

        } catch (Exception e) {
            throw new ProcessException("Can not obtain task[" + taskInstanceId + "] extension attribute", e);
        }

    }

	private final String getTaskRelatedInfo(String taskInstanceId, TaskInformations taskInfo) {
		
		TaskInformations key = taskInfo;
		TaskEntity task = null;
        try {
            task = (TaskEntity) getTaskService().createTaskQuery().taskId(taskInstanceId).singleResult();
        } catch (ActivitiException e) {
            throw new ProcessException("Can't get task instance by giving ID" + taskInstanceId, e);
        }
        final TaskEntity taskEntity = task;
        // Reason may two: one is task ID is invalid, other is task information value is null so not cache it.
        if (taskEntity == null) {
        	logger.info("Task[" + taskInstanceId +"] isn't exists, throw exception.");
        	throw new ProcessException("Can't get task instance by giving ID" + taskInstanceId);
        }

        if (WorkflowOperations.TaskInformations.PROCESS_INSTANCE_ID.equals(key)) {
        	logger.debug("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + task.getProcessInstanceId());
        	return task.getProcessInstanceId();
        }
        if (WorkflowOperations.TaskInformations.TASK_TAG.equals(key)) {
        	logger.debug("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + task.getTaskDefinitionKey());
        	return task.getTaskDefinitionKey();
        }
        if (WorkflowOperations.TaskInformations.TASK_ROLE_TAG.equals(key)) {
        	String taskRoleTag = runExtraCommand(new Command<String>() {

				@Override
				public String execute(CommandContext commandContext) {
					TaskDefinition td = taskEntity.getTaskDefinition();
					return td.getCandidateGroupIdExpressions().iterator().next().getExpressionText();
				}
        		
            });
        	logger.debug("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + taskRoleTag);
        	return taskRoleTag;
        }
        if (WorkflowOperations.TaskInformations.BUSINESS_OBJECT_ID.equals(key)) {
        	// Adapt call activity/sub-process/two combination case
            String rootProcessInstanceId = obtainRootProcess(task.getProcessInstanceId(), true);
            ProcessInstance rootPi = getRuntimeService().createProcessInstanceQuery().processInstanceId(rootProcessInstanceId).singleResult();
            Assert.notNull(rootPi.getBusinessKey(), "Business key must not be null, so this means we need upgrade this code to fix it.");
            logger.debug("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + rootPi.getBusinessKey());
            return rootPi.getBusinessKey();
        }
        if (WorkflowOperations.TaskInformations.CLASSDELEGATE_ADAPTER_TLI.equals(key)) {
        	String classDelegateTli = runExtraCommand(new Command<String>() {

				@Override
				public String execute(CommandContext commandContext) {
					TaskDefinition td = taskEntity.getTaskDefinition();
					if (td.getTaskListeners() != null && td.getTaskListeners().size() > 0) {
                        for (List<TaskListener> value : td.getTaskListeners().values()) {
                            if (value != null && !value.isEmpty()) {
                                for (TaskListener tl : value) {
                                    if (ExtendAttrsClassDelegateAdapter.class.isInstance(tl)) {
                                    	// FIXME: Support single extend attributes holder temporarily
                                    	String tlis = ((ExtendAttrsClassDelegateAdapter) tl).getExtendAttrs().get(ActivitiAccessor.TASK_LIFECYCLE_INTERCEPTOR);
                                        return tlis == null ? "" : tlis;
                                    }
                                }
                            }
                        }
                    }
					// Not found
					return "";
				}
        		
            });
        	logger.debug("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + classDelegateTli);
        	return classDelegateTli;
        }
        
        if (WorkflowOperations.TaskInformations.CLASSDELEGATE_ADAPTER_TOI.equals(key)) {
        	logger.debug("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + "empty string");
        	return "";
        }
        
        if (WorkflowOperations.TaskInformations.FORM_KEY.equals(key)) {
        	
        	String formKey = getFormService().getTaskFormData(taskInstanceId).getFormKey();
        	logger.debug("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + formKey);
        	return formKey == null ? "" : formKey;
        }
        
        if (WorkflowOperations.TaskInformations.TASK_SERVICE_INVOKE_EXPRESSION.equals(key)) {
        	String taskServiceInvokeExp = runExtraCommand(new Command<String>() {

				@Override
				public String execute(CommandContext commandContext) {
					TaskDefinition td = taskEntity.getTaskDefinition();
					if (td.getTaskListeners() != null && td.getTaskListeners().size() > 0) {
                        for (List<TaskListener> value : td.getTaskListeners().values()) {
                            if (value != null && !value.isEmpty()) {
                                for (TaskListener tl : value) {
                                    if (ExtendAttrsClassDelegateAdapter.class.isInstance(tl)) {
                                    	// FIXME: Support single extend attributes holder temporarily
                                    	String values = ((ExtendAttrsClassDelegateAdapter) tl).getExtendAttrs().get(ActivitiAccessor.TASK_SERVICE_INVOKE_EXPRESSION);
                                        return values == null ? "" : values;
                                    }
                                }
                            }
                        }
                    }
					// Not found
					return "";
				}
        		
            });
        	logger.debug("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + taskServiceInvokeExp);
        	return taskServiceInvokeExp;
        }
        
        if (WorkflowOperations.TaskInformations.EXTEND_ATTRIBUTES.equals(key)) {
        	String extendAttributes = runExtraCommand(new Command<String>() {

				@Override
				public String execute(CommandContext commandContext) {
					TaskDefinition td = taskEntity.getTaskDefinition();
					if (td.getTaskListeners() != null && td.getTaskListeners().size() > 0) {
                        for (List<TaskListener> value : td.getTaskListeners().values()) {
                            if (value != null && !value.isEmpty()) {
                                for (TaskListener tl : value) {
                                    if (ExtendAttrsClassDelegateAdapter.class.isInstance(tl)) {
                                    	// FIXME: Support single extend attributes holder temporarily
                                    	String values = XStreamSerializeHelper.serializeXml("extendAttrs", ((ExtendAttrsClassDelegateAdapter) tl).getExtendAttrs());
                                        return values == null ? "" : values;
                                    }
                                }
                            }
                        }
                    }
					// Not found
					return "";
				}
        		
            });
        	logger.debug("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + extendAttributes);
        	return extendAttributes;
        }
        
        if (WorkflowOperations.TaskInformations.TASK_DEFINE_NAME.equals(key)) {
        	logger.debug("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + task.getName());
        	return task.getName() == null ? "" : task.getName();
        }
        
        if (WorkflowOperations.TaskInformations.ROOT_PROCESS_INSTANCE_ID.equals(key)) {
        	String rootProcessInstanceId = obtainRootProcess(task.getProcessInstanceId(), true);
        	logger.debug("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + rootProcessInstanceId);
        	return rootProcessInstanceId;
        }
        
        if (WorkflowOperations.TaskInformations.PROCESS_DEFINE_KEY.equals(key)) {
        	String processDefinitionKey = runExtraCommand(new Command<String>() {

				@Override
				public String execute(CommandContext commandContext) {
					ProcessDefinitionEntity pde = commandContext.getProcessDefinitionManager().findLatestProcessDefinitionById(taskEntity.getProcessDefinitionId());
					return pde.getKey();
				}
        		
            });
        	logger.debug("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + processDefinitionKey);
        	return processDefinitionKey;
        }
        
        throw new UnsupportedOperationException("Unsupported key: " + key);
	}

    public final String obtainRootProcess(String processInstanceId, boolean includeCallActivity) {
    	
    	String rootProcessNotCrossCallActivity = findRootProcessNotCrossCallActivity(processInstanceId);
        ExecutionEntity pi = (ExecutionEntity) getRuntimeService().createProcessInstanceQuery().processInstanceId(rootProcessNotCrossCallActivity).singleResult();
        ExecutionEntity rootEE = pi;
        while (rootEE.getSuperExecutionId() != null) {
        	ExecutionEntity superEE = (ExecutionEntity) getRuntimeService().createExecutionQuery().executionId(pi.getSuperExecutionId()).singleResult();
        	logger.debug("Found super execution entity" + superEE.getId() + ", maybe this task is in callActivity scope.");
        	rootEE = superEE;
        	rootProcessNotCrossCallActivity = obtainRootProcess(superEE.getProcessInstanceId(), true);
        }
        logger.debug("Return root execution entity" + rootEE.getId());
        
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
                logger.debug("Found parent process instance " + ObjectUtils.getDisplayString(pi) + " of " + parentProcessId);
                parentProcessId = pi == null ? null : pi.getProcessInstanceId();
            } catch (ActivitiException e) {
                throw new ProcessException("Can not found local process instance when try to handle sub-process.", e);
            }
        }

        logger.debug("Return root process ID " + rootProcessId + " of " + processInstanceId);
        return rootProcessId;
    }

}
