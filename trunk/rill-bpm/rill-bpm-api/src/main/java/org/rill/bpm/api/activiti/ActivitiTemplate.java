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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Resource;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.activiti.engine.impl.bpmn.data.AbstractDataAssociation;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.juel.IdentifierNode;
import org.activiti.engine.impl.juel.Tree;
import org.activiti.engine.impl.juel.TreeStore;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.rill.bpm.api.TaskExecutionContext;
import org.rill.bpm.api.TaskLifecycleInteceptor;
import org.rill.bpm.api.WorkflowCache;
import org.rill.bpm.api.WorkflowCache.CacheTargetRetriever;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.exception.ProcessException;
import org.rill.bpm.api.support.XpathVarConvertTaskLifecycleInterceptor;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;


/**
 * Activiti implementation of {@link WorkflowOperations}.
 * @author mengran
 */
public class ActivitiTemplate extends ActivitiAccessor implements WorkflowOperations {
	
	@Resource(name="workflowCache")
	private WorkflowCache<HashMap<String, String>> workflowCache;
	
	public final WorkflowCache<HashMap<String, String>> getWorkflowCache() {
		return workflowCache;
	}

	public final void setWorkflowCache(WorkflowCache<HashMap<String, String>> workflowCache) {
		this.workflowCache = workflowCache;
	}
	
    @Override
	public HashMap<String, String> getTaskInstanceInformations(
			final String taskInstanceId) {
		// Delegate to cache
    	return getWorkflowCache().getTaskRelatedInfo(taskInstanceId, new CacheTargetRetriever<HashMap<String, String>>() {

			@Override
			public HashMap<String, String> getCacheTarget(String key) {
				return ActivitiTemplate.super.getTaskInstanceInformations(taskInstanceId);
			}
    		
		});
	}

	@Override
	public HashMap<String, String> getProcessInstanceInformations(
			final String engineProcessInstanceId) {
		// Delegate to cache
		// Delegate to cache
    	return getWorkflowCache().getProcessRelatedInfo(engineProcessInstanceId, new CacheTargetRetriever<HashMap<String, String>>() {

			@Override
			public HashMap<String, String> getCacheTarget(String key) {
				return ActivitiTemplate.super.getProcessInstanceInformations(engineProcessInstanceId);
			}
    		
		});
	}

	// --------------------------------------- Implementation --------------------------//
	@Override
	protected WorkflowResponse doCreateProcessInstance(
			String processDefinitionKey, String processStarter, String businessObjectId,
			Map<String, Object> workflowParams) {
		
		boolean haveSetAuthenticatedUser = false;
		try {
			// Do create process instance of work flow engine
	        UUID taskRetrieveUUID = UUID.randomUUID();
	        RetrieveNextTasksHelper.pushTaskScope(taskRetrieveUUID.toString());
	        
	        // Convert and filter 
	        Set<String> engineRelateDatanames = this.getLastedVersionProcessDefinitionVariableNames(processDefinitionKey);
	        Map<String, Object> passToEngine = XpathVarConvertTaskLifecycleInterceptor.convertAndFilter(engineRelateDatanames, workflowParams);
	        workflowParams.putAll(passToEngine);
	        
	        // Record start user information at 2012-02-07.
	        User processStarterUser = getIdentityService().createUserQuery().userId(processStarter).singleResult();
	        if (processStarterUser == null) {
	        	processStarterUser = getIdentityService().newUser(processStarter);
	        	getIdentityService().saveUser(processStarterUser);
	        }
	        getIdentityService().setAuthenticatedUserId(processStarterUser.getId());
	        haveSetAuthenticatedUser = true;
	        
	        // Do engine operation
	        StringBuilder sb = new StringBuilder();
	        sb.append("Call activiti API for start process instance. ");
	        sb.append(" Params: ");
	        sb.append(ObjectUtils.getDisplayString(passToEngine));
	        logger.info(sb.toString());
	        long startCompleteTime = System.currentTimeMillis();
	        ProcessInstance response = getRuntimeService().startProcessInstanceByKey(processDefinitionKey, businessObjectId, passToEngine);
	        long endCompleteTime = System.currentTimeMillis();
	        logger.info("Activiti response process instance: " + ObjectUtils.getDisplayString(response) + ", createTimeCost: " + (endCompleteTime - startCompleteTime) + " ms]");
	        List<String> taskIds = RetrieveNextTasksHelper.popTaskScope(taskRetrieveUUID.toString());
	        logger.info("Activiti response task instances: " + ObjectUtils.getDisplayString(taskIds));
	        
			return new WorkflowResponse(response.getProcessInstanceId(), businessObjectId, processDefinitionKey, taskIds, obtainRootProcess(response.getProcessInstanceId(), true));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(e);
		} finally {
			// Record start user information at 2012-02-07.
			if (haveSetAuthenticatedUser) {
				getIdentityService().setAuthenticatedUserId(null);
			}
		}
	}

	@Override
	protected void handleTaskInit(List<String> taskListIds, String engineProcessInstanceId, String triggerTaskInstanceId, 
			Object triggerTaskExecutionContext, boolean hasParentProcess, Map<String, Object> workflowParams, String operator) throws ProcessException {
		
		List<Task> taskList = new ArrayList<Task>(taskListIds.size());
        for (String taskId : taskListIds) {
            taskList.add(getTaskService().createTaskQuery().taskId(taskId).singleResult());
        }
        logger.info("Init generated-task" +  ObjectUtils.getDisplayString(taskList));
		
        // Means process will end
        ProcessInstance pi = getRuntimeService().createProcessInstanceQuery().processInstanceId(engineProcessInstanceId).singleResult();
        if (pi == null) {
            // Publish process end event
            publishProcessEndEvent(engineProcessInstanceId, triggerTaskInstanceId, triggerTaskExecutionContext, hasParentProcess);
//            return;
        }

        // Task life cycle initialize method processing
        for (Task response : taskList) {
            TaskLifecycleInteceptor[] newTasklifecycleInteceptors = (TaskLifecycleInteceptor[]) obtainTaskLifecycleInterceptors(response.getId());
            TaskExecutionContext taskExecutionContext = buildTaskExecuteContext(triggerTaskInstanceId, response.getId(), operator, workflowParams);
//            taskExecutionContext.setActivityContentResponse(response);
            logger.debug("Call generated-task''s interceptor#init " + ObjectUtils.getDisplayString(response));
            for (TaskLifecycleInteceptor newTaskLifecycleInteceptor : newTasklifecycleInteceptors) {
                try {
                    // Invoke interceptor's initial method
                    newTaskLifecycleInteceptor.init(taskExecutionContext);
                } catch (Exception e) {
                    // Call work-flow operations exception handler
                    taskLifecycleInterceptorExceptionHandler(e, newTaskLifecycleInteceptor, newTasklifecycleInteceptors);
                    throw new ProcessException(e).setEngineTaskInstanceId(response.getId());
                }
            }
        }
        
	}

    public String getTaskNameByDefineId(final String processDefinitionKey, final String taskDefineId) {

        return runExtraCommand(new Command<String>() {

            public String execute(CommandContext commandContext) {

                ProcessDefinitionEntity pd = Context.getProcessEngineConfiguration().getDeploymentCache().findDeployedLatestProcessDefinitionByKey(processDefinitionKey);
                Assert.notNull(pd, "Can not find process defintion by key[" + processDefinitionKey + "].");
                for (String key : pd.getTaskDefinitions().keySet()) {
                    if (key.equals(taskDefineId)) {
                        return pd.getTaskDefinitions().get(key).getNameExpression().getExpressionText();
                    }
                }

                throw new ProcessException("Can not get task name by task define id[" + taskDefineId + "], processDefinitionKey:" + processDefinitionKey);
            }
        });

    }
    
	@Override
	public String getEngineProcessInstanceIdByBOId(String businessObjectId, String processDefinitionKey)
			throws ProcessException {
		
		ProcessInstance pi = getRuntimeService().createProcessInstanceQuery()
				.processInstanceBusinessKey(businessObjectId, processDefinitionKey).singleResult();
		
		return pi == null ? null : pi.getProcessInstanceId();
	}

    private Set<String> getProcessInstanceVariableNames(final ProcessDefinitionEntity pd) {

    	Assert.notNull(pd, "Process definition entity is null");
    	
        return runExtraCommand(new Command<Set<String>>() {

            public Set<String> execute(CommandContext commandContext) {
                
                Set<String> processAllVariables = new LinkedHashSet<String>();
                List<ActivityImpl> listActivities = ((ScopeImpl) pd).getActivities();
                
                // Deep-first traversale
                deepFirstTraversal(processAllVariables, listActivities);
                
                logger.debug("Found process variables:" + ObjectUtils.getDisplayString(processAllVariables) + ", process definition :" + pd);
                return processAllVariables;
            }
            
            private void deepFirstTraversal(Set<String> processAllVariables, List<ActivityImpl> listActivities) {
                
                for (ActivityImpl ai : listActivities) {
                    
                    if (ai.isScope()) {
                        // Means call-activity
                    	if (ai.getActivityBehavior() instanceof CallActivityBehavior) {
                    		Field dataInputAssociations = ReflectUtil.getField("dataInputAssociations", ai.getActivityBehavior());
                    		dataInputAssociations.setAccessible(true);
                    		@SuppressWarnings("unchecked")
							List<AbstractDataAssociation> list = (List<AbstractDataAssociation>) ReflectionUtils.getField(dataInputAssociations, ai.getActivityBehavior());
                    		if (!CollectionUtils.isEmpty(list)) {
                    			for (AbstractDataAssociation ada : list) {
                    				// Add input associations
                    				processAllVariables.add(ada.getSource());
                    			}
                    		}
                    	} else {
                    		// sub-process
                    		deepFirstTraversal(processAllVariables, ai.getActivities());
                    	}
                        
                    }
                    
                    List<PvmTransition> outTransitions = ai.getOutgoingTransitions();
                    if (outTransitions == null || outTransitions.isEmpty()) {
                        continue;
                    }
                    for (PvmTransition pt : outTransitions) {
                        String contitionText = (String) ((TransitionImpl) pt).getProperty(BpmnParse.PROPERTYNAME_CONDITION_TEXT);
                        if (!StringUtils.hasLength(contitionText)) {
                            continue;
                        }

                        ExpressionManager em = ((ProcessEngineConfigurationImpl) getProcessEngineConfiguration()).getExpressionManager();
                        Field ef = ReflectUtil.getField("expressionFactory", em);
                        ef.setAccessible(true);
                        try {
                            Object efImpl = ef.get(em);
                            Field treeStore = ReflectUtil.getField("store", efImpl);
                            treeStore.setAccessible(true);
                            TreeStore treeStoreObject = (TreeStore) treeStore.get(efImpl);
                            Tree tree = treeStoreObject.get(contitionText);
                            Iterable<IdentifierNode> listIdentifierNode = tree.getIdentifierNodes();
                            Iterator<IdentifierNode> iterator = listIdentifierNode.iterator();
                            while (iterator.hasNext()) {
                                String variableNames = iterator.next().toString();
                                logger.debug("Found process variables:" + variableNames + " on transition:" + pt);
                                processAllVariables.add(variableNames);
                            }
                        } catch (IllegalArgumentException ex) {
                            logger.error("Can not get expression factory object.", ex);
                        } catch (IllegalAccessException ex) {
                            logger.error("Can not get expression factory object.", ex);
                        }
                    }
                }
            }
        });

    }
    
	@Override
	public Set<String> getLastedVersionProcessDefinitionVariableNames(
			final String processDefinitionKey) {
		
		return runExtraCommand(new Command<Set<String>>() {

            public Set<String> execute(CommandContext commandContext) {
            	ProcessDefinitionEntity pd = commandContext.getProcessDefinitionManager().findLatestProcessDefinitionByKey(processDefinitionKey);
            	pd = Context.getProcessEngineConfiguration().getDeploymentCache().findDeployedProcessDefinitionById(pd.getId());
                Assert.notNull(pd, "Can not find process defintion by key[" + processDefinitionKey + "].");
               
                return getProcessInstanceVariableNames(pd);
            }
            
        });
		
	}
	
    public Set<String> getProcessInstanceVariableNames(final String engineProcessInstanceId) {

        return runExtraCommand(new Command<Set<String>>() {

            public Set<String> execute(CommandContext commandContext) {
                // FIXME: sub-process case? call-activity case?
                ExecutionEntity ee = commandContext.getExecutionManager().findExecutionById(engineProcessInstanceId);
                if (!(ee != null && ee.isProcessInstance())) {
                    throw new ProcessException("Can not get process instance by given [" + engineProcessInstanceId + "], or it's not a Activiti ProcessInstance.");
                }
                ProcessDefinitionEntity pd = Context.getProcessEngineConfiguration().getDeploymentCache().findDeployedProcessDefinitionById(ee.getProcessDefinitionId());
                Assert.notNull(pd, "Can not find process defintion by id[" + ee.getProcessDefinitionId() + "].");
               
                return getProcessInstanceVariableNames(pd);
            }
            
        });

    }

    public void resumeProcessInstance(String engineProcessInstanceId,
            String operator, String reason) throws ProcessException {

        // User template method pattern
        this.processInstanceOperationTemplate(engineProcessInstanceId, operator, reason, new ProcessInstanceOperationCallBack() {

            public void doOperation(String processInstanceId,
                    String operator, String reason) throws ActivitiException {
                logger.warn("ACTIVITI5: Unsupported operation" + operationType());
//                throw new ActivitiException("Unsupported operation" + operationType() + ".");
            }

            public PROCESS_OPERATION_TYPE operationType() {
                return PROCESS_OPERATION_TYPE.RESUME;
            }
        });

    }

    public void suspendProcessInstance(String engineProcessInstanceId,
            String operator, String reason) throws ProcessException {

        // User template method pattern
        this.processInstanceOperationTemplate(engineProcessInstanceId, operator, reason, new ProcessInstanceOperationCallBack() {

            public void doOperation(String processInstanceId,
                    String operator, String reason) throws ActivitiException {
                logger.warn("ACTIVITI5: Unsupported operation" + operationType());
//                throw new ActivitiException("Unsupported operation" + operationType() + ".");
            }

            public PROCESS_OPERATION_TYPE operationType() {
                return PROCESS_OPERATION_TYPE.SUSPEND;
            }
        });

    }

    public void terminalProcessInstance(final String engineProcessInstanceId,
            String operator, String reason) throws ProcessException {

        // User template method pattern
        this.processInstanceOperationTemplate(engineProcessInstanceId, operator, reason, new ProcessInstanceOperationCallBack() {

            public void doOperation(String engineProcessInstanceId,
                    String operator, String reason) throws ActivitiException {

                logger.info("ACTIVITI5: Terminal process instance" + engineProcessInstanceId);
                // Do terminal operation
                getRuntimeService().deleteProcessInstance(engineProcessInstanceId, reason);
            }

            public PROCESS_OPERATION_TYPE operationType() {
                return PROCESS_OPERATION_TYPE.TERMINAL;
            }
        });

    }

    // -------------------------------- Task related API ---------------------------------- //

	@Override
	protected WorkflowResponse doCompleteTaskInstance(
			final String engineTaskInstanceId, final String operator, final Map<String, Object> workflowParams) {
		
		try {
	        
	        return runExtraCommand(new Command<WorkflowResponse>() {
	
						@Override
						public WorkflowResponse execute(
								CommandContext commandContext) {
	
							String engineProcessInstanceId = getTaskInstanceInformations(engineTaskInstanceId).get(TaskInformations.PROCESS_INSTANCE_ID.name());
							ExecutionEntity ee = commandContext
									.getExecutionManager().findExecutionById(
											engineProcessInstanceId);
							ProcessDefinitionEntity pde = commandContext
									.getProcessDefinitionManager()
									.findLatestProcessDefinitionById(
											ee.getProcessDefinitionId());
							String processDefinitionKey = pde.getKey();
							long startCompleteTime = System.currentTimeMillis();
					        UUID uuid = UUID.randomUUID();
					        RetrieveNextTasksHelper.pushTaskScope(uuid.toString());
					        
					        // Cache root process instance before completion
					        String rootProcessInstanceId = obtainRootProcess(engineProcessInstanceId, true);
					        
					        // Convert and filter 
					        Set<String> engineRelateDatanames = ActivitiTemplate.this.getProcessInstanceVariableNames(engineProcessInstanceId);
					        Map<String, Object> passToEngine = XpathVarConvertTaskLifecycleInterceptor.convertAndFilter(engineRelateDatanames, workflowParams);
					        workflowParams.putAll(passToEngine);
					        
					        StringBuilder sb = new StringBuilder();
					        sb.append("Call activiti API for complete task instance: ");
					        sb.append(engineTaskInstanceId);
					        sb.append(" Params: ");
					        sb.append(ObjectUtils.getDisplayString(passToEngine));
					        logger.info(sb.toString());
					        getTaskService().claim(engineTaskInstanceId, operator);
					        getTaskService().complete(engineTaskInstanceId, passToEngine);
					        final List<String> taskIds = RetrieveNextTasksHelper.popTaskScope(uuid.toString());
					        long endCompleteTime = System.currentTimeMillis();
					        logger.info("Complete task operation done. [taskInstanceid: " + engineTaskInstanceId + ", operator: " + operator + ", completeTimeCost: " + (endCompleteTime - startCompleteTime) + " ms]");
					        logger.info("Generated tasks: " + ObjectUtils.getDisplayString(taskIds));
					        
							return new WorkflowResponse(
									engineProcessInstanceId, getTaskInstanceInformations(engineTaskInstanceId).get(TaskInformations.BUSINESS_OBJECT_ID.name()),
									processDefinitionKey, taskIds, rootProcessInstanceId);
						}
	
					});
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(e);
		}
        
	}

	@Override
	protected void injectProcessStatus(Object taskExecutionContext,
			List<String> taskList) {
		
		TaskExecutionContext context = (TaskExecutionContext) taskExecutionContext;
		// Means process will end
        ProcessInstance pi = getRuntimeService().createProcessInstanceQuery().processInstanceId(context.getProcessInstanceId()).singleResult();
        if (pi == null) {
            // Set process end flag
        	context.setProcessFinished(true);
        }
		
	}

    public String obtainTaskRole(String engineTaskInstanceId) throws ProcessException {

        return getTaskInstanceInformations(engineTaskInstanceId).get(TaskInformations.TASK_ROLE_TAG.name());
    }

    
    /* (non-Javadoc)
     * @see org.rill.bpm.api.WorkflowOperations#reassignTaskExecuter(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void reassignTaskExecuter(String engineProcessInstanceId, String engineTaskInstanceId,
            String oldExecuter, String newExecuter) throws ProcessException {

        Assert.notNull(engineProcessInstanceId, "processId is null");
        Assert.notNull(engineTaskInstanceId, "taskId is null");
        Assert.notNull(oldExecuter, "oldExecuter is null");
        Assert.notNull(newExecuter, "newExecuter is null");
        try {
            logger.error("ACTIVITI5: Unsupported operation[reassignTaskExecuter]. engineTaskInstanceId " + engineTaskInstanceId);
            throw new ActivitiException("Unsupported operation[reassignTaskExecuter].");
        } catch (ActivitiException e) {
            throw new ProcessException(e);
        }
    }
    
    private AtomicReference<WorkflowOperations> delegate = new AtomicReference<WorkflowOperations>();
    private String delegateBeanName = "workflowAccessor";

	public final String getDelegateBeanName() {
		return delegateBeanName;
	}

	public final void setDelegateBeanName(String delegateBeanName) {
		this.delegateBeanName = delegateBeanName;
	}

	/**
	 * Add by MENGRAN at 2012-03-09 for delegate batch operations.
	 */
	@Override
	public Map<String, List<String>> batchCompleteTaskIntances(
			Map<String, Map<String, Object>> batchDTO, String operator)
			throws ProcessException {
		
		Assert.notEmpty(batchDTO);
        Map<String, List<String>> returnTasks = new LinkedHashMap<String, List<String>>();

        logger.info("Batch complete task instance. Params:" + ObjectUtils.getDisplayString(batchDTO));
        UUID uuid = obtainAccessUUID();
        try {
        	for (Entry<String, Map<String, Object>> element : batchDTO.entrySet()) {

                // Delegate to single-task operation
        		delegate.compareAndSet(null, getBeanFactory().getBean(delegateBeanName, WorkflowOperations.class));
            	returnTasks.put(element.getKey(), delegate.get().completeTaskInstance(element.getKey(), operator, element.getValue()));
            }
        } finally {
            // Release resource
            releaseThreadLocalResource(uuid);
        }
        
        return returnTasks;
		
	}

	@Override
	public int hashCode() {
		
		return getProcessEngine().getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof ActivitiTemplate)) {
			return false;
		}
		
		// FIXME MENGRAN it's OK?
		return this.equals(obj);
	}

	@Override
	public String toString() {
		return "ActivitiTemplate [" + getProcessEngine().getName() + "]";
	}


}
