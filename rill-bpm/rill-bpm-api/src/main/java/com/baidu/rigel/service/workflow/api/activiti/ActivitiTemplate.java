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
package com.baidu.rigel.service.workflow.api.activiti;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.activiti.engine.ActivitiException;
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
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.baidu.rigel.service.workflow.api.TaskExecutionContext;
import com.baidu.rigel.service.workflow.api.TaskLifecycleInteceptor;
import com.baidu.rigel.service.workflow.api.WorkflowOperations;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;

/**
 * Activiti implementation of {@link WorkflowOperations}.
 * @author mengran
 */
public class ActivitiTemplate extends ActivitiAccessor implements WorkflowOperations {

    // --------------------------------------- Implementation --------------------------//
	@Override
	protected WorkflowResponse doCreateProcessInstance(
			String processDefinitionKey, String processStarter, String businessObjectId,
			Map<String, Object> passToEngine) {
		
		try {
			// Do create process instance of work flow engine
	        UUID taskRetrieveUUID = UUID.randomUUID();
	        RetrieveNextTasksHelper.pushTaskScope(taskRetrieveUUID.toString());
	        
	        ProcessInstance response = getRuntimeService().startProcessInstanceByKey(processDefinitionKey, businessObjectId, passToEngine);
	        List<String> taskIds = RetrieveNextTasksHelper.popTaskScope(taskRetrieveUUID.toString());
	        
			return new WorkflowResponse(response.getProcessInstanceId(), businessObjectId, processDefinitionKey, taskIds, obtainRootProcess(response.getProcessInstanceId(), true));
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new ProcessException(e);
		}
	}

    private void handleTaskInit(List<Task> acr, String engineProcessInstanceId, String triggerTaskInstanceId, TaskExecutionContext triggerTaskExecutionContext, boolean hasParentProcess) throws ProcessException {

        // Means process will end
        ProcessInstance pi = getRuntimeService().createProcessInstanceQuery().processInstanceId(engineProcessInstanceId).singleResult();
        if (pi == null) {
            // Publish process end event
            publishProcessEndEvent(engineProcessInstanceId, triggerTaskInstanceId, triggerTaskExecutionContext, hasParentProcess);
            return;
        }

        // Task life cycle initialize method processing
        for (Task response : acr) {
            TaskLifecycleInteceptor[] newTasklifecycleInteceptors = (TaskLifecycleInteceptor[]) obtainTaskLifecycleInterceptors(response.getId());
            TaskExecutionContext taskExecutionContext = buildTaskExecuteContext(triggerTaskInstanceId, response.getId(), null, null);
//            taskExecutionContext.setActivityContentResponse(response);
            logger.log(Level.FINE, "Call generated-task''s interceptor#init {0}", ObjectUtils.getDisplayString(response));
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
    
	@Override
	protected void handleTaskInit(List<String> taskIds,
			String engineProcessInstanceId, String triggerTaskInstanceId,
			Object triggerTaskExecutionContext, boolean hasParentProcess) throws ProcessException {
		
		List<Task> taskList = new ArrayList<Task>(taskIds.size());
        for (String taskId : taskIds) {
            taskList.add(getTaskService().createTaskQuery().taskId(taskId).singleResult());
        }
        logger.log(Level.FINE, "Retrieve generated-task{0}", ObjectUtils.getDisplayString(taskList));
		
        // Delegate this operation
        this.handleTaskInit(taskList, engineProcessInstanceId, triggerTaskInstanceId, (TaskExecutionContext) triggerTaskExecutionContext, hasParentProcess);
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
                Set<String> processAllVariables = new LinkedHashSet<String>();
                List<ActivityImpl> listActivities = ((ScopeImpl) pd).getActivities();
                
                // Deep-first traversale
                deepFirstTraversal(processAllVariables, listActivities);
                
                logger.log(Level.FINE, "Found process variables:{0}, process instance id:{1}", new Object[]{ObjectUtils.getDisplayString(processAllVariables), engineProcessInstanceId});
                return processAllVariables;
            }
            
            private void deepFirstTraversal(Set<String> processAllVariables, List<ActivityImpl> listActivities) {
                
                for (ActivityImpl ai : listActivities) {
                    
                    if (ai.isScope()) {
                        // Means sub-process or call-activity
                        deepFirstTraversal(processAllVariables, ai.getActivities());
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
                                logger.log(Level.FINEST, "Found process variables:{0} on transition:{1}", new Object[]{variableNames, pt});
                                processAllVariables.add(variableNames);
                            }
                        } catch (IllegalArgumentException ex) {
                            logger.log(Level.SEVERE, "Can not get expression factory object.", ex);
                        } catch (IllegalAccessException ex) {
                            logger.log(Level.SEVERE, "Can not get expression factory object.", ex);
                        }
                    }
                }
            }
        });

    }

    public void resumeProcessInstance(String engineProcessInstanceId,
            String operator, String reason) throws ProcessException {

        // User template method pattern
        this.processInstanceOperationTemplate(engineProcessInstanceId, operator, reason, new ProcessInstanceOperationCallBack() {

            public void doOperation(String processInstanceId,
                    String operator, String reason) throws ActivitiException {
                logger.log(Level.SEVERE, "ACTIVITI5: Unsupported operation{0}.", operationType());
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
                logger.log(Level.SEVERE, "ACTIVITI5: Unsupported operation{0}.", operationType());
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

                logger.log(Level.INFO, "ACTIVITI5: Terminal process instance[{0}.", engineProcessInstanceId);
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
			final String engineTaskInstanceId, final String operator, final Map<String, Object> passToEngine) {
		
		try {
	        return runExtraCommand(new Command<WorkflowResponse>() {
	
						@Override
						public WorkflowResponse execute(
								CommandContext commandContext) {
	
							String engineProcessInstanceId = obtainProcessInstanceId(engineTaskInstanceId);
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
					        
					        // Add by MENGRAN at 2011-06-10
					        getTaskService().claim(engineTaskInstanceId, operator);
					        getTaskService().complete(engineTaskInstanceId, passToEngine);
					        final List<String> taskIds = RetrieveNextTasksHelper.popTaskScope(uuid.toString());
					        long endCompleteTime = System.currentTimeMillis();
					        logger.log(Level.INFO, "Complete task operation done. [taskInstanceid: {0}, operator: {1}, timeCost: {2} ms]", new Object[]{engineTaskInstanceId, operator, endCompleteTime - startCompleteTime});
					        
							return new WorkflowResponse(
									engineProcessInstanceId, obtainBusinessObjectId(engineTaskInstanceId),
									processDefinitionKey, taskIds, rootProcessInstanceId);
						}
	
					});
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
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

        return obtainTaskRoleTag(engineTaskInstanceId);
    }

    /* (non-Javadoc)
     * @see com.baidu.rigel.sp.platform.workflow.api.WorkflowOperations#reAssignActivityPerformer(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void reassignTaskExecuter(String engineProcessInstanceId, String engineTaskInstanceId,
            String oldExecuter, String newExecuter) throws ProcessException {

        Assert.notNull(engineProcessInstanceId, "processId is null");
        Assert.notNull(engineTaskInstanceId, "taskId is null");
        Assert.notNull(oldExecuter, "oldExecuter is null");
        Assert.notNull(newExecuter, "newExecuter is null");
        try {
            logger.log(Level.SEVERE, "ACTIVITI5: Unsupported operation[reassignTaskExecuter].");
            throw new ActivitiException("Unsupported operation[reassignTaskExecuter].");
        } catch (ActivitiException e) {
            throw new ProcessException(e);
        }
    }

    public HashMap<String, String> getTaskInstanceExtendAttrs(String engineTaskInstanceId) {

        return getExtendAttrs(engineTaskInstanceId);
    }

}
