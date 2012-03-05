package org.rill.bpm.api;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.api.exception.ProcessException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;


/**
 * Basic template class, define work-flow operation interceptor mechanism.
 * @author mengran
 *
 */
public abstract class WorkflowTemplate implements WorkflowOperations, BeanFactoryAware, ApplicationEventPublisherAware, InitializingBean {

	private static final String THREAD_RESOURCE_SCOPE = WorkflowTemplate.class.getName() + ".THREAD_RESOURCE_SCOPE";
	public static final String TASK_ROLE_TAG = TaskInformations.TASK_ROLE_TAG.name();
	public static final String TASK_DEFINE_ID = TaskInformations.TASK_TAG.name();
	public static final String TASK_FORM_KEY = TaskInformations.FORM_KEY.name();
	public static final String TASK_LIFECYCLE_INTERCEPTOR = "task_lifecycle_interceptor";
    public static final String TASK_SERVICE_INVOKE_EXPRESSION = "taskServiceInvokeExpression";
    public static final String TASK_LIFECYCLE_INTERCEPTOR_DELIM = ",";
	
    public static class WorkflowResponse {
    	
    	String engineProcessInstanceId;
    	String businessObjectId;
    	String processDefinitionKey;
    	
    	List<String> engineTaskInstanceIds;
    	String rootEngineProcessInstanceId;

    	protected WorkflowResponse() {
    	}
    	
		public WorkflowResponse(String engineProcessInstanceId,
				String businessObjectId, String processDefinitionKey,
				List<String> engineTaskInstanceIds, String rootEngineProcessInstanceId) {
			super();
			this.engineProcessInstanceId = engineProcessInstanceId;
			this.businessObjectId = businessObjectId;
			this.processDefinitionKey = processDefinitionKey;
			this.engineTaskInstanceIds = engineTaskInstanceIds;
			this.rootEngineProcessInstanceId = rootEngineProcessInstanceId;
		}

		public final String getRootEngineProcessInstanceId() {
			return rootEngineProcessInstanceId;
		}

		public final void setRootEngineProcessInstanceId(
				String rootEngineProcessInstanceId) {
			this.rootEngineProcessInstanceId = rootEngineProcessInstanceId;
		}

		public final String getEngineProcessInstanceId() {
			return engineProcessInstanceId;
		}

		public final void setEngineProcessInstanceId(String engineProcessInstanceId) {
			this.engineProcessInstanceId = engineProcessInstanceId;
		}

		public final String getBusinessObjectId() {
			return businessObjectId;
		}

		public final void setBusinessObjectId(String businessObjectId) {
			this.businessObjectId = businessObjectId;
		}

		public final String getProcessDefinitionKey() {
			return processDefinitionKey;
		}

		public final void setProcessDefinitionKey(String processDefinitionKey) {
			this.processDefinitionKey = processDefinitionKey;
		}

		public final List<String> getEngineTaskInstanceIds() {
			return engineTaskInstanceIds;
		}

		public final void setEngineTaskInstanceIds(List<String> engineTaskInstanceIds) {
			this.engineTaskInstanceIds = engineTaskInstanceIds;
		}

		@Override
		public String toString() {
			
			return ToStringBuilder.reflectionToString(this);
		}
		
    }
	
	/** Logger available to subclasses */
    protected final Log logger = LogFactory.getLog(getClass().getName());
    
    private List<TaskLifecycleInteceptor> commonTaskLifecycleInterceptor;
    private List<ProcessCreateInteceptor> processCreateInteceptor;
    private List<ProcessOperationInteceptor> processOperationInteceptor;
    private BeanFactory beanFactory;
    private ApplicationEventPublisher applicationEventPublisher;
    
	public final void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public final void setApplicationEventPublisher(
			ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public final BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public final List<ProcessOperationInteceptor> getProcessOperationInteceptor() {
		return processOperationInteceptor;
	}

	public final void setProcessOperationInteceptor(
			List<ProcessOperationInteceptor> processOperationInteceptor) {
		this.processOperationInteceptor = processOperationInteceptor;
	}

	public final List<TaskLifecycleInteceptor> getCommonTaskLifecycleInterceptor() {
		return commonTaskLifecycleInterceptor;
	}

	public final void setCommonTaskLifecycleInterceptor(
			List<TaskLifecycleInteceptor> commonTaskLifecycleInterceptor) {
		this.commonTaskLifecycleInterceptor = commonTaskLifecycleInterceptor;
	}

	public final List<ProcessCreateInteceptor> getProcessCreateInteceptor() {
		return processCreateInteceptor;
	}

	public final void setProcessCreateInteceptor(
			List<ProcessCreateInteceptor> processCreateInteceptor) {
		this.processCreateInteceptor = processCreateInteceptor;
	}
	
    private class ApplicationEventPublisherAdapter implements ApplicationEventPublisher {

        private SimpleApplicationEventMulticaster saemc;

        public ApplicationEventPublisherAdapter(SimpleApplicationEventMulticaster aemc) {
            this.saemc = aemc;
//            this.saemc.setBeanFactory(getBeanFactory());
            String[] listenerNames = ((ListableBeanFactory) beanFactory).getBeanNamesForType(ApplicationListener.class);
            if (listenerNames != null && listenerNames.length > 0) {
                for (String listenerName : listenerNames) {
                    logger.info("Add application listener named " + listenerName + ".");
                    aemc.addApplicationListener((ApplicationListener) beanFactory.getBean(listenerName));
                }
            }
        }

        public void publishEvent(ApplicationEvent event) {

            // Delegate operatoin
            this.saemc.multicastEvent(event);
        }
    }

	@Override
	public void afterPropertiesSet() throws Exception {
        // Generate application context as event publisher
        if (!(beanFactory instanceof ApplicationEventPublisher)) {
            // Means aware method have not been call.
            // FIXME: Adapt spring v2.0 implementation, we use new SimpleApplicationEventMulticaster() but not new SimpleApplicationEventMulticaster(BeanFactory)
            logger.info("Adapt external environment[Not ApplicationContext]." + beanFactory + ".");
            this.applicationEventPublisher = new ApplicationEventPublisherAdapter(new SimpleApplicationEventMulticaster());
        }
	}

	/**
	 * @param processDefinitionKey
	 * @param processStarter
	 * @param businessObjectId
	 * @param workflowParams NOT NULL
	 * @return
	 * @throws ProcessException
	 */
	protected abstract WorkflowResponse doCreateProcessInstance(String processDefinitionKey, String processStarter, String businessObjectId, Map<String, Object> workflowParams) throws ProcessException;
	protected abstract void handleTaskInit(List<String> taskList, String engineProcessInstanceId, String triggerTaskInstanceId, Object triggerTaskExecutionContext, boolean hasParentProcess, Map<String, Object> workflowParams, String operator) throws ProcessException;
	
	protected final UUID obtainAccessUUID() {

        if (ThreadLocalResourceHolder.getProperty(THREAD_RESOURCE_SCOPE) == null) {
            // Means scope start point
            UUID uuid = UUID.randomUUID();
            ThreadLocalResourceHolder.bindProperty(THREAD_RESOURCE_SCOPE, uuid);
            logger.debug("Start thread local resource scope. Cache UUID:" + uuid + ".");
            return uuid;
        } else {
            UUID newUUID = UUID.randomUUID();
            logger.debug("Nested workflow access, return new UUID: " + newUUID + ".");
            return newUUID;
        }
    }
	
	protected final void releaseThreadLocalResource(UUID uuid) {

        Assert.notNull(uuid);
        UUID threadUUID = (UUID) ThreadLocalResourceHolder.getProperty(THREAD_RESOURCE_SCOPE);

        // Compare UUID
        if (uuid == threadUUID || uuid.equals(threadUUID)) {
            logger.info("Clear thread-local resource. UUID given:{" + uuid + "}; thread UUID:{" + threadUUID + "}");
            // Release resource.
            ThreadLocalResourceHolder.getThreadMap().clear();
        } else {
            logger.info("Do not clear thread-local resource. UUID given:{" + uuid + "}; thread UUID:{" + threadUUID + "}");
        }
    }
	
	protected void handleProcessException(ProcessException pe) {
		
		logger.error("Process exception occurred!!! \n ProcessInstanceId: " + ObjectUtils.getDisplayString(pe.getEngineProcessInstanceId()) + 
				"TaskInstanceId: " + ObjectUtils.nullSafeToString(pe.getEngineTaskInstanceId()) + 
				"Process Phase: " + pe.getProcessInterceptorPhase().name() + 
				"Task Phase:" +  pe.getTaskLifecycleInterceptorPhase().name());

        throw pe;
    }
	
	@Override
	public final List<String> createProcessInstance(String processDefinitionKey,
			String processStarter, String businessObjectId,
			Map<String, Object> startParams) throws ProcessException {
		
		// Ensure business object not null
        if (!StringUtils.hasText(businessObjectId)) {
            throw new ProcessException("Parameter[businessObjectId] is null.").setProcessInterceptorPhase(ProcessException.PROCESS_PHASE.BEFORE_CREATE);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(processStarter);
        sb.append(" try to create a process instance of ");
        sb.append(processDefinitionKey);
        sb.append(" with BO key:");
        sb.append(businessObjectId);
        sb.append(" and params: ");
        sb.append(ObjectUtils.getDisplayString(startParams));
        logger.info(sb.toString());
        
        if (startParams == null) {
        	startParams = new HashMap<String, Object>();
        }
        
        UUID uuid = obtainAccessUUID();
        // Call previous operation
        if (getProcessCreateInteceptor() != null && !getProcessCreateInteceptor().isEmpty()) {
            for (ProcessCreateInteceptor pci : getProcessCreateInteceptor()) {
                try {
                    pci.preOperation(processDefinitionKey, processStarter, businessObjectId, startParams);
                } catch (ProcessException pe) {
                    // Release thread-local resource
                    releaseThreadLocalResource(uuid);

                    // Throw exception for terminal process creation
                    throw pe.setProcessInterceptorPhase(ProcessException.PROCESS_PHASE.BEFORE_CREATE).setBoId(businessObjectId.toString());
                }
            }
        }
        if (!StringUtils.hasText(processDefinitionKey)) {
            // Release thread-local resource
            releaseThreadLocalResource(uuid);
            throw new ProcessException("Fail to create process, because processDefinitionKey is null. "
                    + "And you can set it " + ProcessCreateInteceptor.class.getName()).setProcessInterceptorPhase(ProcessException.PROCESS_PHASE.BEFORE_CREATE).setBoId(businessObjectId.toString());
        }

        // Call engine service to create a process
        WorkflowResponse response = null;
        try {
            response = doCreateProcessInstance(processDefinitionKey, processStarter, businessObjectId, startParams);
            sb = new StringBuilder();
            sb.append(processStarter);
            sb.append(" created process instance response: ");
            sb.append(ObjectUtils.getDisplayString(response));
            logger.info(sb.toString());

            // Call post operation
            if (getProcessCreateInteceptor() != null && !getProcessCreateInteceptor().isEmpty()) {
                // Reverse list
                List<ProcessCreateInteceptor> reverseList = new ArrayList<ProcessCreateInteceptor>();
                reverseList.addAll(getProcessCreateInteceptor());
                Collections.reverse(reverseList);
                logger.debug("Process create interceptor after reverse " + ObjectUtils.getDisplayString(reverseList));
                for (ProcessCreateInteceptor pci : reverseList) {
                    // Do post operation
                    try {
                        pci.postOperation(processDefinitionKey, response.getEngineProcessInstanceId(), businessObjectId, processStarter);
                    } catch (ProcessException pe) {
                        throw pe.setBoId(response.getBusinessObjectId()).setEngineProcessInstanceId(response.getEngineProcessInstanceId()).setProcessInterceptorPhase(ProcessException.PROCESS_PHASE.POST_CREATE);
                    }
                }
            }

            // Handle task initialize phase
            try {
                handleTaskInit(response.getEngineTaskInstanceIds(), response.getEngineProcessInstanceId(), 
                		null, null, !response.getEngineProcessInstanceId().equals(response.getRootEngineProcessInstanceId()), startParams, processStarter);
            } catch (ProcessException e) {
                throw e.setProcessInterceptorPhase(ProcessException.PROCESS_PHASE.POST_CREATE).setTaskLifecycleInterceptorPhase(ProcessException.TASK_LIFECYCLE_PHASE.INIT).setBoId(response.getBusinessObjectId()).setEngineProcessInstanceId(response.getEngineProcessInstanceId());
            }
        } catch (ProcessException pdte) {
            // Call exception handle procedure
            handleProcessException(pdte);
        } finally {
            // Release resource
            releaseThreadLocalResource(uuid);
        }

        return response.getEngineTaskInstanceIds();
        
	}
	
    /**
    *
    * Process operation call-back
    */
   public interface ProcessInstanceOperationCallBack {

       /**
        * @return Operation type
        */
       WorkflowOperations.PROCESS_OPERATION_TYPE operationType();

       void doOperation(String engineProcessInstanceId,
               String operator, String reason) throws ProcessException;
   }
   
    /**
     * Process operation template method.
     * @param engineProcessInstanceId engine process instance ID
     * @param operator operator
     * @param reason Reason
     * @param callback Call back
     * @throws ProcessException Exception when execute operation
     */
    protected void processInstanceOperationTemplate(String engineProcessInstanceId,
            String operator, String reason, ProcessInstanceOperationCallBack callback) throws ProcessException {

        UUID uuid = obtainAccessUUID();
        
        StringBuilder sb = new StringBuilder();
        sb.append(operator);
        sb.append(" try to " + callback.operationType().name() + " a process instance of ");
        sb.append(engineProcessInstanceId);
        sb.append(" with reason:");
        sb.append(ObjectUtils.getDisplayString(reason));
        logger.info(sb.toString());
        
        // Call previous operation
        if (getProcessOperationInteceptor() != null && !getProcessOperationInteceptor().isEmpty()) {
            for (ProcessOperationInteceptor poi : getProcessOperationInteceptor()) {
                try {
                    // Throw NullPointerException if not implementation rightly
                    if (poi.handleOpeationType().equals(callback.operationType())) {
                        poi.preOperation(engineProcessInstanceId, operator, reason);
                    }
                } catch (ProcessException pe) {
                    // Release resource
                    releaseThreadLocalResource(uuid);
                    throw new ProcessException("Fail to do " + callback.operationType().name()
                            + " on process instance[" + engineProcessInstanceId + pe.getMessage(), pe).setProcessInterceptorPhase(ProcessException.PROCESS_PHASE.BEFORE_OPERATION).setOperator(operator);
                }
            }
        }

        // Access engine
        try {
            callback.doOperation(engineProcessInstanceId, operator, reason);

            // Call post operation
            if (getProcessOperationInteceptor() != null && !getProcessOperationInteceptor().isEmpty()) {
                // Reverse list
                List<ProcessOperationInteceptor> reverseList = new ArrayList<ProcessOperationInteceptor>();
                reverseList.addAll(getProcessOperationInteceptor());
                Collections.reverse(reverseList);
                for (ProcessOperationInteceptor poi : reverseList) {
                    // Throw NullPointerException if not implementation rightly
                    if (poi.handleOpeationType().equals(callback.operationType())) {
                        try {
                            poi.postOperation(engineProcessInstanceId);
                        } catch (ProcessException pe) {
                            throw new ProcessException("Fail to do " + callback.operationType().name()
                                    + " on process instance[" + engineProcessInstanceId + pe.getMessage(), pe).setProcessInterceptorPhase(ProcessException.PROCESS_PHASE.POST_OPERATION).setOperator(operator);
                        }

                    }
                }
            }

        } catch (ProcessException pdte) {
            // Call exception handle procedure
            handleProcessException(pdte);
        } finally {
            // Release resource
            releaseThreadLocalResource(uuid);
        }

    }
	
    protected TaskExecutionContext buildTaskExecuteContext(String triggerTaskInstanceId, String engineTaskInstanceId,
            String operator, Map<String, Object> workflowParams) {

        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();

        // Set properties
        taskExecutionContext.setProcessInstanceId(obtainProcessInstanceId(engineTaskInstanceId));
        taskExecutionContext.setTaskInstanceId(engineTaskInstanceId);
//        taskExecutionContext.setCurrentTask(getTaskService().createTaskQuery().taskId(engineTaskInstanceId).singleResult());
        taskExecutionContext.setTaskExtendAttributes(getTaskInformations(engineTaskInstanceId));
        taskExecutionContext.setWorkflowParams(workflowParams);
        taskExecutionContext.setOperator(operator);
        taskExecutionContext.setPreTaskInstanceId(triggerTaskInstanceId);
        taskExecutionContext.setTaskDefineName(taskExecutionContext.getTaskExtendAttributes().get(TaskInformations.TASK_DEFINE_NAME.name()));
        // Set BO ID
        taskExecutionContext.setBusinessObjectId(obtainBusinessObjectId(engineTaskInstanceId));

        // Set task related informations
        taskExecutionContext.setTaskTag(obtainTaskTag(engineTaskInstanceId));
        taskExecutionContext.setTaskRoleTag(obtainTaskRoleTag(engineTaskInstanceId));

//        // Set sub process flag
//        taskExecutionContext.setSubProcess(hasParentProcess(taskExecutionContext.getProcessInstanceId()));
        taskExecutionContext.setRootProcessInstanceId(taskExecutionContext.getTaskExtendAttributes().get(TaskInformations.ROOT_PROCESS_INSTANCE_ID.name()));
        
//        // Set process instance end flag
//        ProcessInstance pi = getRuntimeService().createProcessInstanceQuery().processInstanceId(taskExecutionContext.getProcessInstanceId()).singleResult();
//        taskExecutionContext.setProcessFinished(pi == null);

        if (taskExecutionContext.getWorkflowParams() == null) {
            taskExecutionContext.setWorkflowParams(new HashMap<String, Object>());
        }

        return taskExecutionContext;
    }
    
	protected abstract WorkflowResponse doCompleteTaskInstance(String engineTaskInstanceId, String operator, Map<String, Object> passToEngine) throws ProcessException;
    protected abstract void injectProcessStatus(Object taskExecutionContext, List<String> taskList);

    protected void taskLifecycleInterceptorExceptionHandler(Exception e, TaskLifecycleInteceptor exceptionMurderer,
            TaskLifecycleInteceptor[] tasklifecycleInteceptors) {

        Assert.notNull(e);
        if (tasklifecycleInteceptors == null || tasklifecycleInteceptors.length == 0) {
            logger.error("No task lifecycle interceptor, but who call this method for exception handler.");
            return;
        }

        logger.info("Call task lifecycle inteceptor''s exception handle method.{" + ObjectUtils.getDisplayString(tasklifecycleInteceptors) + "} for murderer:{" + exceptionMurderer + "}");
        for (TaskLifecycleInteceptor tli : tasklifecycleInteceptors) {
            try {
                tli.onExceptionOccurred(e, exceptionMurderer);
            } catch (Exception ex) {
                logger.warn("WorkflowOperationsExceptionHandler#onCompleteTaskInstanceException not allow throws any exception.", ex);
            }
        }

    }
    
    public final List<String> completeTaskInstance(String engineTaskInstanceId, String operator, Map<String, Object> workflowParams) throws ProcessException {

    	Assert.notNull(engineTaskInstanceId);
    	
    	StringBuilder sb = new StringBuilder();
        sb.append(operator);
        sb.append(" try to complete a task instance of ");
        sb.append(engineTaskInstanceId);
        sb.append(" with params: ");
        sb.append(ObjectUtils.getDisplayString(workflowParams));
        logger.info(sb.toString());
    	
        UUID uuid = obtainAccessUUID();
        try {
            // Delegate this operation
            return handleCompleteTaskInstance(engineTaskInstanceId, operator, workflowParams);
        } finally {
            // Release resource
            releaseThreadLocalResource(uuid);
        }

    }

    protected List<String> handleCompleteTaskInstance(String engineTaskInstanceId,
            String operator, Map<String, Object> workflowParams) throws ProcessException {
            
        // Build task execution context
        Object taskExecutionContext = buildTaskExecuteContext(null, engineTaskInstanceId, operator, workflowParams);

        // Do operation at previous Web Service calling for distribute transaction
        TaskLifecycleInteceptor[] tasklifecycleInteceptors = obtainTaskLifecycleInterceptors(engineTaskInstanceId);

        // Dynamic work flow parameters holder
        Map<String, Object> workflowParamsDynamic = new HashMap<String, Object>();
        // Call previous operation
        if (tasklifecycleInteceptors != null && tasklifecycleInteceptors.length != 0) {
            for (TaskLifecycleInteceptor tasklifecycleInteceptor : tasklifecycleInteceptors) {
                try {
                    // Invoke interceptor's logic
                    workflowParamsDynamic = tasklifecycleInteceptor.preComplete(taskExecutionContext);
                } catch (ProcessException pe) {
                    // Call work-flow operations exception handler
                    taskLifecycleInterceptorExceptionHandler(pe, tasklifecycleInteceptor, tasklifecycleInteceptors);
                    throw new ProcessException("Fail to complete task id:" + engineTaskInstanceId, pe).setEngineTaskInstanceId(engineTaskInstanceId).setTaskLifecycleInterceptorPhase(ProcessException.TASK_LIFECYCLE_PHASE.PRE_COMPLETE).setOperator(operator);
                }
            }
        }
        
        // Filter engine-driven DTO is nesessery
        workflowParamsDynamic.remove(ENGINE_DRIVEN_TASK_FORM_DATA_KEY);

        logger.info("Complete task:" + engineTaskInstanceId + ", with workflow params: " + ObjectUtils.getDisplayString(workflowParamsDynamic));
                
        // Access engine
        WorkflowResponse response = null;
        try {
        	// Do engine complete task instance
        	response = doCompleteTaskInstance(engineTaskInstanceId, operator, workflowParamsDynamic);
        	StringBuilder sb = new StringBuilder();
            sb.append(operator);
            sb.append(" completed task instance response: ");
            sb.append(ObjectUtils.getDisplayString(response));
            logger.info(sb.toString());
            // Analyze process status, and inject into context
            injectProcessStatus(taskExecutionContext, response.getEngineTaskInstanceIds());

            // Call post operation
            if (tasklifecycleInteceptors != null && tasklifecycleInteceptors.length != 0) {
                Object[] reverseArray = ArrayUtils.clone(tasklifecycleInteceptors);
                ArrayUtils.reverse(reverseArray);
                for (TaskLifecycleInteceptor tasklifecycleInteceptor : (TaskLifecycleInteceptor[]) reverseArray) {
                    try {
                        // Invoke interceptor's logic
                        tasklifecycleInteceptor.postComplete(taskExecutionContext);
                    } catch (Exception pe) {
                        // Call work-flow operations exception handler
                        taskLifecycleInterceptorExceptionHandler(pe, tasklifecycleInteceptor, tasklifecycleInteceptors);
                        throw new ProcessException(pe).setEngineTaskInstanceId(engineTaskInstanceId)
                        	.setTaskLifecycleInterceptorPhase(ProcessException.TASK_LIFECYCLE_PHASE.POST_COMPLETE).setOperator(operator);
                    }
                }
            }

            // Handle task initialize event
            try {
                handleTaskInit(response.getEngineTaskInstanceIds(), obtainProcessInstanceId(engineTaskInstanceId), 
                		engineTaskInstanceId, taskExecutionContext, !response.getEngineProcessInstanceId().equals(response.getRootEngineProcessInstanceId()),
                		workflowParamsDynamic, operator);
            } catch (ProcessException pe) {
                // Call work-flow operations exception handler -- Do it In handleTaskInit method.
//				workflowOperationsExceptionHandlerInvoke(e, tasklifecycleInteceptor);
                throw pe.setOperator(operator);
            }

            // Call after complete operation
            if (tasklifecycleInteceptors != null && tasklifecycleInteceptors.length != 0) {
                for (TaskLifecycleInteceptor tasklifecycleInteceptor : tasklifecycleInteceptors) {
                    try {
                        // Invoke interceptor's logic
                        tasklifecycleInteceptor.afterComplete(taskExecutionContext);
                    } catch (Exception e) {
                        // Call work-flow operations exception handler
                        taskLifecycleInterceptorExceptionHandler(e, tasklifecycleInteceptor, tasklifecycleInteceptors);
                        throw new ProcessException(e).setTaskLifecycleInterceptorPhase(ProcessException.TASK_LIFECYCLE_PHASE.AFTER_COMPLETE).setEngineTaskInstanceId(engineTaskInstanceId).setOperator(operator);
                    }
                }
            }

        } catch (ProcessException pdte) {
            // Call exception handle procedure
            handleProcessException(pdte);
        } finally {
            // Move to outer
            // Release resource
            // releaseThreadLocalResource();
        }
        
        return response.getEngineTaskInstanceIds();
    }

    // Remote final by MENGRAN.
    public Map<String, List<String>> batchCompleteTaskIntances(Map<String, Map<String, Object>> batchDTO, String operator) throws ProcessException {

        Assert.notEmpty(batchDTO);
        Map<String, List<String>> returnTasks = new LinkedHashMap<String, List<String>>();

        logger.info("Batch complete task instance. Params:" + ObjectUtils.getDisplayString(batchDTO));
        UUID uuid = obtainAccessUUID();
        try {
        	for (Entry<String, Map<String, Object>> element : batchDTO.entrySet()) {

                // Delegate to single-task operation
            	returnTasks.put(element.getKey(), this.handleCompleteTaskInstance(element.getKey(), operator, element.getValue()));
            }
        } finally {
            // Release resource
            releaseThreadLocalResource(uuid);
        }
        
        return returnTasks;

    }
    
    protected void publishProcessEndEvent(String processInstanceId, String triggerTaskInstanceId, Object triggerTaskExecutionContext, boolean hasParentProcess) {

        logger.info("Process instance[" + processInstanceId + "] end. Trigger task[" + triggerTaskInstanceId + "]. Has parent process: " + hasParentProcess);
        this.applicationEventPublisher.publishEvent(new ProcessInstanceEndEvent(processInstanceId,
                triggerTaskInstanceId, hasParentProcess, triggerTaskExecutionContext));
    }
    
    protected final String[] obtainCommaSplitSpecifyValues(String taskInstanceId, String extendsAttributeKey, String delim) {

        Map<String, String> adArray = getTaskInformations(taskInstanceId);
        if (adArray == null || adArray.isEmpty()) {
            logger.debug("Return empty array because extend attribute is empty.");
            return new String[0];
        }

        Set<String> valueSet = new LinkedHashSet<String>();
        for (Entry<String, String> entry : adArray.entrySet()) {
            if (extendsAttributeKey.equals(entry.getKey())) {
                logger.debug("Match entry for key[" + extendsAttributeKey + "], value[" + entry.getValue() + "].");
                String[] interceptorData = StringUtils.delimitedListToStringArray(StringUtils.trimAllWhitespace(entry.getValue()), delim);
                if (interceptorData != null && interceptorData.length > 0) {
                    valueSet.addAll(Arrays.asList(interceptorData));
                }
            }
        }

        logger.debug("PARSING EXTEND ATTRS--Task[" + taskInstanceId + "] extension attribute key[" + extendsAttributeKey + "]:" + ObjectUtils.getDisplayString(valueSet));
        return valueSet.toArray(new String[valueSet.size()]);
    }
    
    protected abstract String obtainCacheInfos(String taskInstanceId, TaskInformations cacheInfo);
    
    @SuppressWarnings("unchecked")
	protected HashMap<String, String> getTaskInformations(String taskInstanceId) {

        try {
            Map<String, String> extendAttrsMap = new HashMap<String, String>();
            
            // Put cache informations into extend attributes map
            for (TaskInformations ti : TaskInformations.values()) {
            	extendAttrsMap.put(ti.name(), obtainCacheInfos(taskInstanceId, ti));
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
    
    protected String obtainProcessInstanceId(String taskInstanceId) {

        return obtainCacheInfos(taskInstanceId, TaskInformations.PROCESS_INSTANCE_ID);
    }

    protected String obtainTaskTag(String taskInstanceId) {

        return obtainCacheInfos(taskInstanceId, TaskInformations.TASK_TAG);
    }

    protected String obtainTaskRoleTag(String taskInstanceId) {

        return obtainCacheInfos(taskInstanceId, TaskInformations.TASK_ROLE_TAG);
    }

    protected String obtainBusinessObjectId(String taskInstanceId) {

        return obtainCacheInfos(taskInstanceId, TaskInformations.BUSINESS_OBJECT_ID);
    }
    
    protected final TaskLifecycleInteceptor[] obtainTaskLifecycleInterceptors(String taskInstanceId) {

        Collection<TaskLifecycleInteceptor> taskLifecycleInterceptors = new LinkedHashSet<TaskLifecycleInteceptor>();
        
        // Add common TLI configuration
        if (this.getCommonTaskLifecycleInterceptor() != null && !this.getCommonTaskLifecycleInterceptor().isEmpty()) {
            logger.debug("Combin common task-lifecycle-interceptor: " + ObjectUtils.getDisplayString(this.getCommonTaskLifecycleInterceptor()));
            taskLifecycleInterceptors.addAll(getCommonTaskLifecycleInterceptor());
        }
        
        String[] perTaskInterceptorConfigs = obtainCommaSplitSpecifyValues(taskInstanceId, TASK_LIFECYCLE_INTERCEPTOR, TASK_LIFECYCLE_INTERCEPTOR_DELIM);
        List<TLIGenerator> tempGeneratorList = new ArrayList<TLIGenerator>(1);
        tempGeneratorList.add(DEFAULT_SPRING_TLI_GENERATOR);
        for (TLIGenerator generator : tempGeneratorList) {
            if (generator.supportGeneratePattern(perTaskInterceptorConfigs)) {
                taskLifecycleInterceptors.addAll(generator.generate(perTaskInterceptorConfigs));
            } else {
            	logger.warn(generator.getClass().getName() + " can not generate TLI " + ObjectUtils.getDisplayString(perTaskInterceptorConfigs));
            }
        }

        logger.debug("Return task[" + taskInstanceId + "] task-lifecycle-interceptor:" + ObjectUtils.getDisplayString(taskLifecycleInterceptors));
        return taskLifecycleInterceptors.toArray(new TaskLifecycleInteceptor[taskLifecycleInterceptors.size()]);
    }
    
    private final TLIGenerator<TaskLifecycleInteceptor> DEFAULT_SPRING_TLI_GENERATOR = new SpringBeanTLIGenerator();
    
    private class SpringBeanTLIGenerator extends SpringBeanGenerator<TaskLifecycleInteceptor> {
        
    }
    
    private abstract class SpringBeanGenerator<T> implements TLIGenerator<T> {

        protected Class<T> actualClazz;

        @SuppressWarnings("unchecked")
		public SpringBeanGenerator() {
            actualClazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        }

        public Collection<T> generate(String[] source) {

            return convertNameToBean(actualClazz, source);
        }

        public boolean supportGeneratePattern(String[] source) {

            boolean result = true;
            try {
                convertNameToBean(actualClazz, source);
            } catch (Exception e) {
                logger.warn(SpringBeanGenerator.class.getName() + "#supportGeneratePattern exception, ignore it." + ObjectUtils.getDisplayString(source));
                result = false;
            }

            return result;
        }

        @SuppressWarnings("unchecked")
        private Collection<T> convertNameToBean(Class<T> clazz, String[] beanName) {

            if (ObjectUtils.isEmpty(beanName)) {
                logger.debug("Return empty array because bean names is empty.");
                return new ArrayList<T>(0);
            }

            LinkedHashSet<T> beanList = new LinkedHashSet<T>(beanName.length);
            for (String name : beanName) {
                String[] interceptorData = StringUtils.commaDelimitedListToStringArray(StringUtils.trimAllWhitespace(name));
                for (String interceptor: interceptorData) {
                    Assert.isTrue(getBeanFactory().containsBean(interceptor), "Bean name[" + interceptor + "] may not be registed in Spring bean factory.");
                    logger.debug("Find bean named[" + interceptor + "] and add it for return.");
                    beanList.add((T) getBeanFactory().getBean(interceptor));
                }
            }

            logger.debug("Convert bean names[" + ObjectUtils.getDisplayString(beanName) + "] to beans " +  ObjectUtils.getDisplayString(beanList));
            return beanList;
        }
    }

}
