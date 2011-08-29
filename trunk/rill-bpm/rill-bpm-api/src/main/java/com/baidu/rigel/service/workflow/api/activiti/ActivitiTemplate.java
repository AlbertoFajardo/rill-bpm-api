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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.baidu.rigel.service.workflow.api.ThreadLocalResourceHolder;
import com.baidu.rigel.service.workflow.api.ProcessCreateInteceptor;
import com.baidu.rigel.service.workflow.api.ProcessOperationInteceptor;
import com.baidu.rigel.service.workflow.api.TaskLifecycleInteceptor;
import com.baidu.rigel.service.workflow.api.WorkflowOperations;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.jws.WebService;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.interceptor.Command;
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
import org.springframework.util.StringUtils;

/**
 * Activiti implementation of {@link WorkflowOperations}.
 * @author mengran
 */
@WebService(endpointInterface="com.baidu.rigel.service.workflow.api.WorkflowOperations")
public class ActivitiTemplate extends ActivitiAccessor implements WorkflowOperations {

    private static final String THREAD_RESOURCE_SCOPE = ActivitiTemplate.class.getName() + ".THREAD_RESOURCE_SCOPE";

    // --------------------------------------- Implementation --------------------------//
    public void createProcessInstance(CreateProcessInstanceDto createProcessInstanceDto) throws ProcessException {
        
        String processDefinitionKey = createProcessInstanceDto.getProcessDefinitionKey();
        String processStarter = createProcessInstanceDto.getProcessStarter();
        String businessObjectId = createProcessInstanceDto.getBusinessObjectId();
        Map<String, String> startParams = createProcessInstanceDto.getStartParams();
        
        // Ensure business object not null
        if (businessObjectId == null) {
            throw new ProcessException("Parameter[businessObjectId] is null.").setProcessInterceptorPhase(ProcessException.PROCESS_PHASE.BEFORE_CREATE);
        }
        
        Map<String, Object> passToEngine = new HashMap<String, Object>();
        if (startParams != null) {
            for (Entry<String, String> entry : startParams.entrySet()) {
                passToEngine.put(entry.getKey(), entry.getValue());
            }
        } else {
            passToEngine = null;
        }

        UUID uuid = obtainAccessUUID();
        // Call previous operation
        if (getProcessCreateInteceptor() != null && !getProcessCreateInteceptor().isEmpty()) {
            for (ProcessCreateInteceptor pci : getProcessCreateInteceptor()) {
                try {
                    pci.preOperation(processDefinitionKey, processStarter, businessObjectId, passToEngine);
                } catch (ProcessException pe) {
                    // Release thread-local resource
                    releaseThreadLocalResource(uuid);

                    // Throw exception for terminal process creation
                    throw pe.setProcessInterceptorPhase(ProcessException.PROCESS_PHASE.BEFORE_CREATE).setBoId(businessObjectId.toString());
                }
            }
        }
        if (processDefinitionKey == null) {
            // Release thread-local resource
            releaseThreadLocalResource(uuid);
            throw new ProcessException("Fail to create process, because processDefinitionKey is null. "
                    + "And you can set it " + ProcessCreateInteceptor.class.getName()).setProcessInterceptorPhase(ProcessException.PROCESS_PHASE.BEFORE_CREATE).setBoId(businessObjectId.toString());
        }

        // Call engine service to create a process
        ProcessInstance response = null;
        try {
            // Do create process instance of work flow engine
            UUID taskRetrieveUUID = UUID.randomUUID();
            RetrieveNextTasksHelper.pushTaskScope(taskRetrieveUUID.toString());
            
            response = getRuntimeService().startProcessInstanceByKey(processDefinitionKey, businessObjectId, passToEngine);
            List<String> taskIds = RetrieveNextTasksHelper.popTaskScope(taskRetrieveUUID.toString());

            // Call post operation
            if (getProcessCreateInteceptor() != null && !getProcessCreateInteceptor().isEmpty()) {
                // Reverse list
                List<ProcessCreateInteceptor> reverseList = new ArrayList<ProcessCreateInteceptor>();
                reverseList.addAll(getProcessCreateInteceptor());
                Collections.reverse(reverseList);
                logger.log(Level.FINE, "Process create interceptor after reverse {0}", ObjectUtils.getDisplayString(reverseList));
                for (ProcessCreateInteceptor pci : reverseList) {
                    // Do post operation
                    try {
                        pci.postOperation(response.getProcessInstanceId(), businessObjectId, processStarter);
                    } catch (ProcessException pe) {
                        throw pe.setBoId(response.getBusinessKey()).setEngineProcessInstanceId(response.getProcessInstanceId()).setProcessInterceptorPhase(ProcessException.PROCESS_PHASE.POST_CREATE);
                    }
                }
            }

            // Handle task initialize phase
            try {
                List<Task> taskList = new ArrayList<Task>(taskIds.size());
                for (String taskId : taskIds) {
                    taskList.add(getTaskService().createTaskQuery().taskId(taskId).singleResult());
                }
                logger.log(Level.FINE, "Retrieve generated-task{0}", ObjectUtils.getDisplayString(taskList));
                handleTaskInit(taskList, response.getProcessInstanceId(), null, null);
            } catch (ProcessException e) {
                throw e.setProcessInterceptorPhase(ProcessException.PROCESS_PHASE.POST_CREATE).setTaskLifecycleInterceptorPhase(ProcessException.TASK_LIFECYCLE_PHASE.INIT).setBoId(response.getBusinessKey()).setEngineProcessInstanceId(response.getProcessInstanceId());
            }
        } catch (ActivitiException e) {
            throw new ProcessException("Fail to create process: " + e.getMessage(), e).setBoId(businessObjectId.toString()).setProcessInterceptorPhase(ProcessException.PROCESS_PHASE.ENGINE_OPERATION);
        } catch (ProcessException pdte) {
            // Call exception handle procedure
            handleProcessException(pdte);
        } finally {
            // Release resource
            releaseThreadLocalResource(uuid);
        }

//        return response;
    }

    private void releaseThreadLocalResource(UUID uuid) {

        Assert.notNull(uuid);
        UUID threadUUID = (UUID) ThreadLocalResourceHolder.getProperty(THREAD_RESOURCE_SCOPE);

        // Compare UUID
        if (uuid == threadUUID || uuid.equals(threadUUID)) {
            logger.log(Level.INFO, "Clear thread-local resource. UUID given:{0}; thread UUID:{1}", new Object[]{uuid, threadUUID});
            // Release resource.
            ThreadLocalResourceHolder.getThreadMap().clear();
        } else {
            logger.log(Level.INFO, "Do not clear thread-local resource. UUID given:{0}; thread UUID:{1}", new Object[]{uuid, threadUUID});
        }
    }

    private UUID obtainAccessUUID() {

        if (ThreadLocalResourceHolder.getProperty(THREAD_RESOURCE_SCOPE) == null) {
            // Means scope start point
            UUID uuid = UUID.randomUUID();
            ThreadLocalResourceHolder.bindProperty(THREAD_RESOURCE_SCOPE, uuid);
            logger.log(Level.FINE, "Start thread local resource scope. Cache UUID:{0}", uuid);
            return uuid;
        } else {
            UUID newUUID = UUID.randomUUID();
            logger.log(Level.FINE, "Nested workflow access, return new UUID{0}", newUUID);
            return newUUID;
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

    /**
     *
     * Process operation call-back
     */
    private interface ProcessInstanceOperationCallBack {

        /**
         * @return Operation type
         */
        WorkflowOperations.PROCESS_OPERATION_TYPE operationType();

        void doOperation(String engineProcessInstanceId,
                String operator, String reason) throws ActivitiException;
    }

    /**
     * Process operation template method.
     * @param engineProcessInstanceId engine process instance ID
     * @param operator operator
     * @param reason Reason
     * @param callback Call back
     * @throws ProcessException Exception when execute operation
     */
    private void processInstanceOperationTemplate(String engineProcessInstanceId,
            String operator, String reason, ProcessInstanceOperationCallBack callback) throws ProcessException {

        UUID uuid = obtainAccessUUID();
        // Call previous operation
        if (getProcessOperationInteceptors() != null && !getProcessOperationInteceptors().isEmpty()) {
            for (ProcessOperationInteceptor poi : getProcessOperationInteceptors()) {
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
            if (getProcessOperationInteceptors() != null && !getProcessOperationInteceptors().isEmpty()) {
                // Reverse list
                List<ProcessOperationInteceptor> reverseList = new ArrayList<ProcessOperationInteceptor>();
                reverseList.addAll(getProcessOperationInteceptors());
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

        } catch (ActivitiException e) {
            throw new ProcessException("Fail to do " + callback.operationType().name() + " on process instance[" + engineProcessInstanceId + "].", e).setProcessInterceptorPhase(ProcessException.PROCESS_PHASE.POST_OPERATION).setOperator(operator);
        } catch (ProcessException pdte) {
            // Call exception handle procedure
            handleProcessException(pdte);
        } finally {
            // Release resource
            releaseThreadLocalResource(uuid);
        }

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
    public void batchCompleteTaskIntances(List<CompleteTaskInstanceDto> batchDTO) throws ProcessException {

        Assert.notEmpty(batchDTO);

        logger.log(Level.INFO, "Batch complete task instance. Params:{0}", ObjectUtils.getDisplayString(batchDTO));
        for (CompleteTaskInstanceDto element : batchDTO) {

            // Delegate to single-task operation
            this.doCompleteTaskInstance(element.getEngineTaskInstanceId(), element.getOperator(), element.getWorkflowParams());
        }

    }

    protected ActivitiTaskExecutionContext buildTaskExecuteContext(String triggerTaskInstanceId, String engineTaskInstanceId,
            String operator, Map<String, Object> workflowParams) {

        ActivitiTaskExecutionContext taskExecutionContext = new ActivitiTaskExecutionContext();

        // Set properties
        taskExecutionContext.setProcessInstanceId(obtainProcessInstanceId(engineTaskInstanceId));
        taskExecutionContext.setTaskInstanceId(engineTaskInstanceId);
        taskExecutionContext.setCurrentTask(getTaskService().createTaskQuery().taskId(engineTaskInstanceId).singleResult());
        taskExecutionContext.setTaskExtendAttributes(getExtendAttrs(engineTaskInstanceId));
        taskExecutionContext.setWorkflowParams(workflowParams);
        taskExecutionContext.setOperator(operator);
        taskExecutionContext.setPreTaskInstanceId(triggerTaskInstanceId);
        // Set BO ID
        taskExecutionContext.setBusinessObjectId(obtainBusinessObjectId(engineTaskInstanceId));

        // Set task related informations
        taskExecutionContext.setTaskTag(obtainTaskTag(engineTaskInstanceId));
        taskExecutionContext.setTaskRoleTag(obtainTaskRoleTag(engineTaskInstanceId));

        // Set sub process flag
        taskExecutionContext.setSubProcess(hasParentProcess(taskExecutionContext.getProcessInstanceId()));

        if (taskExecutionContext.getWorkflowParams() == null) {
            taskExecutionContext.setWorkflowParams(new HashMap<String, Object>());
        }

        return taskExecutionContext;
    }

    public void completeTaskInstance(CompleteTaskInstanceDto completeTaskInstanceDto) throws ProcessException {

        UUID uuid = obtainAccessUUID();
        try {
            // Delegate this operation
            doCompleteTaskInstance(completeTaskInstanceDto.getEngineTaskInstanceId(), completeTaskInstanceDto.getOperator(), completeTaskInstanceDto.getWorkflowParams());
        } finally {
            // Release resource
            releaseThreadLocalResource(uuid);
        }

    }

    protected void doCompleteTaskInstance(String engineTaskInstanceId,
            String operator, Map<String, String> workflowParams) throws ProcessException {

        logger.log(Level.INFO, "Complete task instance. Params:{0}", ObjectUtils.getDisplayString(workflowParams));
        
        Map<String, Object> passToEngine = new HashMap<String, Object>();
        if (workflowParams != null) {
            for (Entry<String, String> entry : workflowParams.entrySet()) {
                passToEngine.put(entry.getKey(), entry.getValue());
            }
        } else {
            passToEngine = null;
        }
            
        // Build task execution context
        ActivitiTaskExecutionContext taskExecutionContext = buildTaskExecuteContext(null, engineTaskInstanceId, operator, passToEngine);

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
        // Merge work flow parameters
        if (passToEngine == null) {
            passToEngine = new HashMap<String, Object>();
        }
        passToEngine.putAll(workflowParamsDynamic);
        
        // Filter engine-driven DTO is nessesary
        passToEngine.remove(ENGINE_DRIVEN_TASK_FORM_DATA_KEY);

        logger.log(Level.INFO, "Complete task:{0}, with workflow params:{1}", new Object[]{engineTaskInstanceId, ObjectUtils.getDisplayString(passToEngine)});

        // Access engine
        try {
            List<String> taskIds = null;
            long startCompleteTime = System.currentTimeMillis();
            UUID uuid = UUID.randomUUID();
            RetrieveNextTasksHelper.pushTaskScope(uuid.toString());
            // Add by MENGRAN at 2011-06-10
            getTaskService().claim(engineTaskInstanceId, operator);
            getTaskService().complete(engineTaskInstanceId, passToEngine);
            taskIds = RetrieveNextTasksHelper.popTaskScope(uuid.toString());
            long endCompleteTime = System.currentTimeMillis();
            logger.log(Level.INFO, "Complete task operation done. [taskInstanceid: {0}, operator: {1}, timeCost: {2} ms]", new Object[]{engineTaskInstanceId, operator, endCompleteTime - startCompleteTime});
            // Get new generated tasks
            List<Task> taskList = new ArrayList<Task>(taskIds.size());
            for (String taskId : taskIds) {
                taskList.add(getTaskService().createTaskQuery().taskId(taskId).singleResult());
            }
            // Analyze process status, and inject into context
            injectProcessStatus(taskExecutionContext, taskList);

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
                        throw new ProcessException(pe).setEngineTaskInstanceId(engineTaskInstanceId).setTaskLifecycleInterceptorPhase(ProcessException.TASK_LIFECYCLE_PHASE.POST_COMPLETE).setOperator(operator);
                    }
                }
            }

            // Handle task initialize event
            try {
                handleTaskInit(taskList, obtainProcessInstanceId(engineTaskInstanceId), engineTaskInstanceId, taskExecutionContext);
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

        } catch (ActivitiException e) {
            throw new ProcessException("Fail to complete task[" + engineTaskInstanceId + "].", e).setTaskLifecycleInterceptorPhase(ProcessException.TASK_LIFECYCLE_PHASE.ENGINE_OPERATION).setEngineTaskInstanceId(engineTaskInstanceId).setOperator(operator);
        } catch (ProcessException pdte) {
            // Call exception handle procedure
            handleProcessException(pdte);
        } finally {
            // Move to outer
            // Release resource
            // releaseThreadLocalResource();
        }

    }

    protected void taskLifecycleInterceptorExceptionHandler(Exception e, TaskLifecycleInteceptor exceptionMurderer,
            TaskLifecycleInteceptor[] tasklifecycleInteceptors) {

        Assert.notNull(e);
        if (tasklifecycleInteceptors == null || tasklifecycleInteceptors.length == 0) {
            logger.severe("No task lifecycle interceptor, but who call this method for exception handler.");
            return;
        }

        logger.log(Level.INFO, "Call task lifecycle inteceptor''s exception handle method.{0} for murderer:{1}", new Object[]{ObjectUtils.getDisplayString(tasklifecycleInteceptors), exceptionMurderer});
        for (TaskLifecycleInteceptor tli : tasklifecycleInteceptors) {
            try {
                tli.onExceptionOccurred(e, exceptionMurderer);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "WorkflowOperationsExceptionHandler#onCompleteTaskInstanceException not allow throws any exception.", ex);
            }
        }

    }

    protected void handleProcessException(ProcessException pe) {

        logger.log(Level.SEVERE, "Process exception occurred!!! \n" + "ProcessInstanceId[{0}"
                + "]," + "TaskInstanceId[{1}" + "],"
                + "Process Phase[{2}" + "]," + "Task Phase[{3}].",
                new Object[]{ObjectUtils.getDisplayString(pe.getEngineProcessInstanceId()),
                    ObjectUtils.nullSafeToString(pe.getEngineTaskInstanceId()),
                    pe.getProcessInterceptorPhase().name(),
                    pe.getTaskLifecycleInterceptorPhase().name()});

        throw pe;
    }

    private void injectProcessStatus(ActivitiTaskExecutionContext taskExecutionContext, List<Task> acr) {

        // Means process will end
        ProcessInstance pi = getRuntimeService().createProcessInstanceQuery().processInstanceId(taskExecutionContext.getProcessInstanceId()).singleResult();
        if (pi == null) {
            // Set process end flag
            taskExecutionContext.setProcessFinished(true);
        }

    }

    protected void handleTaskInit(List<Task> acr, String engineProcessInstanceId, String triggerTaskInstanceId, ActivitiTaskExecutionContext triggerTaskExecutionContext) throws ProcessException {

        // Means process will end
        ProcessInstance pi = getRuntimeService().createProcessInstanceQuery().processInstanceId(engineProcessInstanceId).singleResult();
        if (pi == null) {
            // Publish process end event
            publishProcessEndEvent(engineProcessInstanceId, triggerTaskInstanceId, triggerTaskExecutionContext);
            return;
        }

        // Task life cycle initialize method processing
        for (Task response : acr) {
            TaskLifecycleInteceptor[] newTasklifecycleInteceptors = (TaskLifecycleInteceptor[]) obtainTaskLifecycleInterceptors(response.getId());
            ActivitiTaskExecutionContext taskExecutionContext = buildTaskExecuteContext(triggerTaskInstanceId, response.getId(), null, null);
            taskExecutionContext.setActivityContentResponse(response);
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

    public void abortTaskInstance(String engineTaskInstanceId) throws ProcessException {

        try {
            // getBpmServiceClient().abortActivity(obtainProcessInstanceId(taskInstanceId), taskInstanceId);
            logger.log(Level.SEVERE, "ACTIVITI5: Unsupported operation.");
            throw new ActivitiException("Unsupported operation");
        } catch (ActivitiException e) {
            throw new ProcessException("Fail to abort task instance.", e);
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
