package com.baidu.rigel.service.workflow.api.activiti;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.baidu.rigel.service.workflow.api.exception.ProcessDistributeTransactionException;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.juel.IdentifierNode;
import org.activiti.engine.impl.juel.Tree;
import org.activiti.engine.impl.juel.TreeStore;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.util.ReflectUtil;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * 公司名：百度 <br>
 * 系统名：Rigel直销系统<br>
 * 子系统名: <br>
 * 模块名：HT-SUPPORT <br>
 * 文件名：ActivitiTemplate.java<br>
 * 功能说明: Activiti工作流操作实现类。使用Activiti本地工作流API接口。<br>
 * @author mengran
 * @version 1.0.0
 * @date 2010-5-14下午04:32:06
 **/
public class ActivitiTemplate extends ActivitiAccessor implements WorkflowOperations {

    private static final String THREAD_RESOURCE_SCOPE = ActivitiTemplate.class.getName() + ".THREAD_RESOURCE_SCOPE";

    // --------------------------------------- Implementation --------------------------//
    public Object createProcessInstance(Object modelInfo,
            Object processStarterInfo, Long businessObjectId,
            Map<String, Object> startParams) throws ProcessException {

        // Ensure business object not null
        if (businessObjectId == null) {
            throw new ProcessException("Parameter[businessObjectId] is null.");
        }

        UUID uuid = obtainAccessUUID();
        String cpRequest = (modelInfo instanceof String) ? modelInfo.toString() : null;
        // Call previous operation
        if (getProcessCreateInteceptor() != null && !getProcessCreateInteceptor().isEmpty()) {
            for (ProcessCreateInteceptor pci : getProcessCreateInteceptor()) {
                try {
                    cpRequest = (String) pci.preOperation(modelInfo, processStarterInfo, businessObjectId, startParams);
                } catch (ProcessException pe) {
                    // Release thread-local resource
                    releaseThreadLocalResource(uuid);

                    // Throw exception for terminal process creation
                    throw new ProcessException("Fail to create process:" + pe.getMessage(), pe);
                }
            }
        }
        if (cpRequest == null) {
            // Release thread-local resource
            releaseThreadLocalResource(uuid);
            throw new ProcessException("Fail to create process, because processDefinitionKey is null. And you can set it though [modelInfo] parameter simply, or using " + ProcessCreateInteceptor.class.getName());
        }

        // Call GWFP service to create a process
        ProcessInstance response = null;
        try {
            // Do create process instance of work flow engine
            UUID taskRetrieveUUID = UUID.randomUUID();
            RetrieveNextTasksHelper.pushTaskScope(taskRetrieveUUID.toString());
            response = getRuntimeService().startProcessInstanceByKey(cpRequest, businessObjectId.toString(), startParams);
            List<String> taskIds = RetrieveNextTasksHelper.popTaskScope(taskRetrieveUUID.toString());

            // Call post operation
            if (getProcessCreateInteceptor() != null && !getProcessCreateInteceptor().isEmpty()) {
                // Reverse list
                List<ProcessCreateInteceptor> reverseList = new ArrayList<ProcessCreateInteceptor>();
                reverseList.addAll(getProcessCreateInteceptor());
                Collections.reverse(reverseList);
                logger.fine("Process create interceptor after reverse " + ObjectUtils.getDisplayString(reverseList));
                for (ProcessCreateInteceptor pci : reverseList) {
                    // Do post operation
                    try {
                        pci.postOperation(response, businessObjectId, processStarterInfo);
                    } catch (Exception e) {
                        throw new ProcessDistributeTransactionException("null",
                                response.getProcessInstanceId(), null,
                                ProcessDistributeTransactionException.EXCEPTION_TRIGGER_ACTION.START_PROCESS_INSTANCE, e);
                    }
                }
            }

            // Handle task initialize event
            try {
                List<Task> taskList = new ArrayList<Task>(taskIds.size());
                for (String taskId : taskIds) {
                    taskList.add(getTaskService().createTaskQuery().taskId(taskId).singleResult());
                }
                logger.fine("Retrieve generated-task" + ObjectUtils.getDisplayString(taskList));
                handleTaskInit(taskList, response.getProcessInstanceId(), null, null);
            } catch (Exception e) {
                throw new ProcessDistributeTransactionException("null",
                        response.getProcessInstanceId(), null,
                        ProcessDistributeTransactionException.EXCEPTION_TRIGGER_ACTION.INIT_TASK_INSTANCE, e);
            }
        } catch (ActivitiException e) {
            throw new ProcessException("Fail to create process: " + e.getMessage(), e);
        } catch (ProcessDistributeTransactionException pdte) {
            logger.severe("Occurred distribute transaction exception. we try to rollback it.");

            // Call exception handle procedure
            handleProcessDistributeTransactionException(pdte);
        } finally {
            // Release resource
            releaseThreadLocalResource(uuid);
        }

        return response;
    }

    private void releaseThreadLocalResource(UUID uuid) {

        Assert.notNull(uuid);
        UUID threadUUID = (UUID) ThreadLocalResourceHolder.getProperty(THREAD_RESOURCE_SCOPE);

        // Compare UUID
        if (uuid == threadUUID || uuid.equals(threadUUID)) {
            logger.info("Clear thread-local resource. UUID given:" + uuid + "; thread UUID:" + threadUUID);
            // Release resource.
            ThreadLocalResourceHolder.getThreadMap().clear();
        } else {
            logger.info("Do not clear thread-local resource. UUID given:" + uuid + "; thread UUID:" + threadUUID);
        }
    }

    private UUID obtainAccessUUID() {

        if (ThreadLocalResourceHolder.getProperty(THREAD_RESOURCE_SCOPE) == null) {
            // Means scope start point
            UUID uuid = UUID.randomUUID();
            ThreadLocalResourceHolder.bindProperty(THREAD_RESOURCE_SCOPE, uuid);
            logger.fine("Start thread local resource scope. Cache UUID:" + uuid);
            return uuid;
        } else {
            UUID newUUID = UUID.randomUUID();
            logger.fine("Nested workflow access, return new UUID" + newUUID);
            return newUUID;
        }
    }

    public String getTaskNameByDefineId(final String processDefinitionKey, final String taskDefineId) {

        return runExtraCommand(new Command<String>() {

            public String execute(CommandContext commandContext) {

                ProcessDefinitionEntity pd = commandContext.getRepositorySession().findDeployedLatestProcessDefinitionByKey(processDefinitionKey);
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
                ExecutionEntity ee = commandContext.getRuntimeSession().findExecutionById(engineProcessInstanceId);
                if (!(ee != null && ee.isProcessInstance())) {
                    throw new ProcessException("Can not get process instance by given [" + engineProcessInstanceId + "], or it's not a Activiti ProcessInstance.");
                }
                ProcessDefinitionEntity pd = commandContext.getRepositorySession().findDeployedProcessDefinitionById(ee.getProcessDefinitionId());
                Assert.notNull(pd, "Can not find process defintion by id[" + ee.getProcessDefinitionId() + "].");
                Set<String> processAllVariables = new LinkedHashSet<String>();
                List<ActivityImpl> listActivities = ((ScopeImpl) pd).getActivities();
                for (ActivityImpl ai : listActivities) {
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
                                logger.finest("Found process variables:" + variableNames + " on transition:" + pt);
                                processAllVariables.add(variableNames);
                            }
                        } catch (IllegalArgumentException ex) {
                            logger.log(Level.SEVERE, "Can not get expression factory object.", ex);
                        } catch (IllegalAccessException ex) {
                            logger.log(Level.SEVERE, "Can not get expression factory object.", ex);
                        }
                    }
                }
                logger.fine("Found process variables:" + ObjectUtils.getDisplayString(processAllVariables) + ", process instance id:" + engineProcessInstanceId);
                return processAllVariables;
            }
        });

    }

    /**
     *
     * 流程操作Call-Back类
     */
    private interface ProcessInstanceOperationCallBack {

        /**
         * @return 流程操作类型
         */
        WorkflowOperations.PROCESS_OPERATION_TYPE operationType();

        void doOperation(String engineProcessInstanceId,
                String operator, String reason) throws ActivitiException;
    }

    /**
     * 流程操作模板方法
     * @param engineProcessInstanceId 流程实例ID
     * @param operator 操作者
     * @param reason 原因
     * @param callback 流程操作CallBack防范
     * @throws ProcessException 工作流异常
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
                            + " on process instance[" + engineProcessInstanceId + pe.getMessage(), pe);
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
                    try {
                        // Throw NullPointerException if not implementation rightly
                        if (poi.handleOpeationType().equals(callback.operationType())) {
                            poi.postOperation(engineProcessInstanceId);
                        }
                    } catch (Exception e) {
                        throw new ProcessDistributeTransactionException(operator, engineProcessInstanceId, null,
                                ProcessDistributeTransactionException.convertWorkflowOperation(callback.operationType()), e);
                    }
                }
            }

        } catch (ActivitiException e) {
            throw new ProcessException("Fail to do " + callback.operationType().name() + " on process instance[" + engineProcessInstanceId + "].", e);
        } catch (ProcessDistributeTransactionException pdte) {
            logger.severe("Occurred distribute transaction exception. we try to rollback it.");
            // Call exception handle procedure
            handleProcessDistributeTransactionException(pdte);
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
                logger.log(Level.SEVERE, "ACTIVITI5: Unsupported operation" + operationType() + ".");
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
                logger.log(Level.SEVERE, "ACTIVITI5: Unsupported operation" + operationType() + ".");
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

                logger.info("ACTIVITI5: Terminal process instance[" + engineProcessInstanceId + ".");
                // Do terminal operation
                getRuntimeService().deleteProcessInstance(engineProcessInstanceId, reason);
            }

            public PROCESS_OPERATION_TYPE operationType() {
                return PROCESS_OPERATION_TYPE.TERMINAL;
            }
        });

    }

//    public void terminalProcessOfGWFP(String engineProcessInstanceId, String operator, String reason) throws ProcessException {
//        try {
//            logger.info("ACTIVITI5: DO NOTHING WHEN [RESUME PROCESS-INSTANCE] OPERATION.");
////			ProcessStatus processStatusOfGWFP = getProcessStateFromGWFP(engineProcessInstanceId);
////			if(processStatusOfGWFP != null && !ProcessStatus.terminaled.equals(processStatusOfGWFP)){
//////				this.getBpmServiceClient().terminalProcess(engineProcessInstanceId, operator, reason);
////				logger.info("ACTIVITI5: DO NOTHING WHEN [RESUME PROCESS-INSTANCE] OPERATION.");
////			}
//        } catch (ActivitiException e) {
//            throw new ProcessException("终止流程失败：GWFP异常。流程Id：" + engineProcessInstanceId, e);
//        }
//    }
    // -------------------------------- Task related API ---------------------------------- //
    public void batchCompleteTaskIntances(
            LinkedHashMap<String, Map<String, Object>> batchDTO,
            String opeartor) throws ProcessException {

        Assert.notEmpty(batchDTO);

        logger.info("Batch complete task instance. Params:" + ObjectUtils.getDisplayString(batchDTO));
        for (Entry<String, Map<String, Object>> entry : batchDTO.entrySet()) {

            // Delegate to single-task operation
            this.doCompleteTaskInstance(entry.getKey(), opeartor, entry.getValue());
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

    public void completeTaskInstance(String engineTaskInstanceId,
            String operator, Map<String, Object> workflowParams) throws ProcessException {

        UUID uuid = obtainAccessUUID();
        try {
            // Delegate this operation
            doCompleteTaskInstance(engineTaskInstanceId, operator, workflowParams);
        } finally {
            // Release resource
            releaseThreadLocalResource(uuid);
        }

    }

    protected void doCompleteTaskInstance(String engineTaskInstanceId,
            String operator, Map<String, Object> workflowParams) throws ProcessException {

        logger.info("Complete task instance. Params:" + ObjectUtils.getDisplayString(workflowParams));

        // Build task execution context
        ActivitiTaskExecutionContext taskExecutionContext = buildTaskExecuteContext(null, engineTaskInstanceId, operator, workflowParams);

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
                    throw new ProcessException("Fail to complete task id:" + engineTaskInstanceId, pe);
                }
            }
        }
        // Merge work flow parameters
        if (workflowParams == null) {
            workflowParams = new HashMap<String, Object>();
        }
        workflowParams.putAll(workflowParamsDynamic);

        // If disallow Seriable varialble, filter all non-primitive or wrapper variable
        // FIXME: Remove all non-primitive variables, but it's maybe include service task expression.
        if (!isSerializeVarPermission()) {
            // Filter all non-primitive or wrapper variable before access work-flow
            Set<String> nonPri = new HashSet<String>();
            for (Entry<String, Object> entry : workflowParams.entrySet()) {
                if (!ClassUtils.isPrimitiveOrWrapper(entry.getValue().getClass())) {
                    logger.finest("Find not primitive or it's wrapper object[key=" + entry.getKey() + "], we will discard it for performance.");
                    nonPri.add(entry.getKey());
                }
            }
            for (String nonPrimitive : nonPri) {
                workflowParams.remove(nonPrimitive);
            }
        }
        // Filter engine-driven DTO is nessesary
        workflowParams.remove(ENGINE_DRIVEN_TASK_FORM_DATA_KEY);
        
        logger.info("Complete task:" + engineTaskInstanceId + ", with workflow params:" + ObjectUtils.getDisplayString(workflowParams));

        // Access engine
        try {
            List<String> taskIds = null;
            long startCompleteTime = System.currentTimeMillis();
            UUID uuid = UUID.randomUUID();
            RetrieveNextTasksHelper.pushTaskScope(uuid.toString());
            // Add by MENGRAN at 2011-06-10
            getTaskService().claim(engineTaskInstanceId, operator);
            getTaskService().complete(engineTaskInstanceId, workflowParams);
            taskIds = RetrieveNextTasksHelper.popTaskScope(uuid.toString());
            long endCompleteTime = System.currentTimeMillis();
            logger.info("Complete task operation done. [taskInstanceid: "
                    + engineTaskInstanceId + ", operator: " + operator + ", timeCost: " + (endCompleteTime - startCompleteTime) + " ms]");
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

                        throw new ProcessDistributeTransactionException(operator, obtainProcessInstanceId(engineTaskInstanceId), engineTaskInstanceId,
                                ProcessDistributeTransactionException.EXCEPTION_TRIGGER_ACTION.COMPLETE_TASK_INSTANCE, pe);
                    }
                }
            }

            // Handle task initialize event
            try {
                handleTaskInit(taskList, obtainProcessInstanceId(engineTaskInstanceId), engineTaskInstanceId, taskExecutionContext);
            } catch (Exception e) {
                // Call work-flow operations exception handler -- Do it In handleTaskInit method.
//				workflowOperationsExceptionHandlerInvoke(e, tasklifecycleInteceptor);
                throw new ProcessDistributeTransactionException(operator, obtainProcessInstanceId(engineTaskInstanceId), engineTaskInstanceId,
                        ProcessDistributeTransactionException.EXCEPTION_TRIGGER_ACTION.INIT_TASK_INSTANCE, e);
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
                        throw new ProcessDistributeTransactionException(operator, obtainProcessInstanceId(engineTaskInstanceId), engineTaskInstanceId,
                                ProcessDistributeTransactionException.EXCEPTION_TRIGGER_ACTION.AFTER_COMPLETE, e);
                    }
                }
            }

        } catch (ActivitiException e) {
            throw new ProcessException("Fail to complete task[" + engineTaskInstanceId + "].", e);
        } catch (ProcessDistributeTransactionException pdte) {
            logger.severe("Occurred distribute transaction exception. we try to rollback it." + pdte);

            // Call exception handle procedure
            handleProcessDistributeTransactionException(pdte);
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

        logger.info("Call task lifecycle inteceptor's exception handle method." + ObjectUtils.getDisplayString(tasklifecycleInteceptors) + " for murderer:" + exceptionMurderer);
        for (TaskLifecycleInteceptor tli : tasklifecycleInteceptors) {
            try {
                tli.onExceptionOccurred(e, exceptionMurderer);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "WorkflowOperationsExceptionHandler#onCompleteTaskInstanceException not allow throws any exception.", ex);
            }
        }

    }

    protected void handleProcessDistributeTransactionException(ProcessDistributeTransactionException pdte) {

        logger.severe("Distribute transaction exception occurred!!! \n"
                + "ProcessInstanceId[" + pdte.getProcessInstanceId() + "],"
                + "TaskInstanceId[" + ObjectUtils.nullSafeToString(pdte.getTaskInstanceId()) + "],"
                + "Trigger Event[" + pdte.getEta().name() + "].");

        if (getDistributeTransactionMessage() == null) {
            logger.warning("No configured distributeTransactionMessage, do not send message.");
        } else {
            Map<String, Object> model = new HashMap<String, Object>();
            try {
                try {
                    InetAddress localHostInfo = InetAddress.getLocalHost();
                    model.put("hostInfo", localHostInfo.getHostName() + " " + localHostInfo.getHostAddress());
                } catch (Exception e) {
                    model.put("hostInfo", "未知");
                }
                model.put("processInstanceId", pdte.getProcessInstanceId());
                model.put("taskInstanceId", ObjectUtils.nullSafeToString(pdte.getTaskInstanceId()));
                model.put("workflowOperation", pdte.getEta().toString());
                model.put("operator", pdte.getOperator());
                if (pdte.getTaskInstanceId() != null) {
                    model.put("boId", obtainBusinessObjectId(pdte.getTaskInstanceId()));
                } else {
                    model.put("boId", "null");
                }
                StringWriter sw = new StringWriter(50 * 1000);
                PrintWriter pw = new PrintWriter(sw);
                pdte.printStackTrace(pw);
                model.put("exception", sw.toString());
                sw = null;
                pw = null;
                getTemplateMailSender().sendMimeMeesage(getDistributeTransactionMessage(),
                        org.springframework.util.StringUtils.hasText(getDistributeTransactionMailTemplate())
                        ? getDistributeTransactionMailTemplate() : "distributeTransactionMailTemplate.ftl", model);
            } catch (Exception e) {
                logger.warning("Exception occurred when try to send email." + e);
            }
        }

        throw new ProcessException("Sorry, distributed transaction exception occurred and have notice related developer.", pdte);
    }

    private void injectProcessStatus(ActivitiTaskExecutionContext taskExecutionContext, List<Task> acr) {

        // Means process will end
        ProcessInstance pi = getRuntimeService().createProcessInstanceQuery().processInstanceId(taskExecutionContext.getProcessInstanceId()).singleResult();
        if (pi == null) {
            // Set process end flag
            taskExecutionContext.setProcessFinished(true);
        }

    }

    protected void handleTaskInit(List<Task> acr, String engineProcessInstanceId, String triggerTaskInstanceId, ActivitiTaskExecutionContext triggerTaskExecutionContext) {

        // Means process will end
        ProcessInstance pi = getRuntimeService().createProcessInstanceQuery().processInstanceId(engineProcessInstanceId).singleResult();
        if (pi == null) {
            // Publish process end event
            publishProcessEndEvent(engineProcessInstanceId, triggerTaskInstanceId, triggerTaskExecutionContext);
            return;
        }

        // Task life cycle initialize method processing
        for (Task response : acr) {
//			// Sub-process end, we log it
//			if (response.isProcessFinish()) {
//				if (hasParentProcess(engineProcessInstanceId))
//					logger.info("Sub-process has end. ProcessInstance:[" + engineProcessInstanceId + "].");
//				else
//					throw new IllegalArgumentException("Main process has end, but not handle by code above[publishProcessEndEvent].");
//				continue;
//			}

            TaskLifecycleInteceptor[] newTasklifecycleInteceptors = (TaskLifecycleInteceptor[]) obtainTaskLifecycleInterceptors(response.getId());
            ActivitiTaskExecutionContext taskExecutionContext = buildTaskExecuteContext(triggerTaskInstanceId, response.getId(), null, null);
            taskExecutionContext.setActivityContentResponse(response);
            logger.fine("Call generated-task's interceptor#init " + ObjectUtils.getDisplayString(response));
            for (TaskLifecycleInteceptor newTaskLifecycleInteceptor : newTasklifecycleInteceptors) {
                try {
                    // Invoke interceptor's initial method
                    newTaskLifecycleInteceptor.init(taskExecutionContext);
                } catch (Exception e) {
                    // Call work-flow operations exception handler
                    taskLifecycleInterceptorExceptionHandler(e, newTaskLifecycleInteceptor, newTasklifecycleInteceptors);
                    throw new ProcessException(e);
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
    public void reassignActivityPerformer(String engineProcessInstanceId, String engineTaskInstanceId,
            String srcPerformer, String newPerformer) throws ProcessException {

        Assert.notNull(engineProcessInstanceId, "processId is null");
        Assert.notNull(engineTaskInstanceId, "taskId is null");
        Assert.notNull(srcPerformer, "src performer is null");
        Assert.notNull(newPerformer, "new performer is null");
        try {
            logger.log(Level.SEVERE, "ACTIVITI5: Unsupported operation[reassignActivityPerformer].");
            throw new ActivitiException("Unsupported operation[reassignActivityPerformer].");
        } catch (ActivitiException e) {
            throw new ProcessException(e);
        }
    }

    public Map<String, String> getTaskInstanceExtendAttrs(String engineTaskInstanceId) {

        return getExtendAttrs(engineTaskInstanceId);
    }
}
