package com.baidu.rigel.service.workflow.api.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.baidu.rigel.service.workflow.api.WorkflowOperations;
import com.baidu.rigel.service.workflow.api.WorkflowOperations.XStreamSerializeHelper;
import com.baidu.rigel.service.workflow.api.WorkflowTemplate.TaskInformations;
import com.baidu.rigel.service.workflow.api.activiti.ActivitiAccessor;
import com.baidu.rigel.service.workflow.api.activiti.ExtendAttrsClassDelegateAdapter;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;

public class TaskInstanceRelatedInfoCache {

	/** Logger available to subclasses */
    protected final Logger logger = Logger.getLogger(getClass().getName());
    
	private Map<String, String[]> taskInstanceInfoCache = new HashMap<String, String[]>();
	private ConcurrentMap<String, Map<String, String>> taskExecutionInfoCache = new ConcurrentHashMap<String, Map<String,String>>();
	
	private WorkflowOperations workflowAccessor;
	private ActivitiAccessor activitiAccessor;
	
    public final WorkflowOperations getWorkflowAccessor() {
		return workflowAccessor;
	}

	public final void setWorkflowAccessor(WorkflowOperations workflowAccessor) {
		this.workflowAccessor = workflowAccessor;
		
		// Need initialize activiti accessor
		activitiAccessor = ActivitiAccessor.retrieveActivitiAccessorImpl(workflowAccessor, ActivitiAccessor.class);
	}
	
	public String getTaskExecutionInfo(String taskInstanceId, String key) {
		
        if (taskExecutionInfoCache.containsKey(taskInstanceId)) {
            String cacheHit = taskExecutionInfoCache.get(taskInstanceId).get(key);
            logger.log(Level.FINE, "Hit execution cache of task instance id[{0}], return {1} for {2}", new Object[]{taskInstanceId, cacheHit, key});
            return cacheHit;
        }
        
        logger.log(Level.FINE, "Not hit execution cache of task instance id[{0}], return null for {1}", new Object[]{taskInstanceId, key});
        return null;
	}
	
	public void putTaskExecutionInfo(String taskInstanceId, String key, String value) {
		
		if (taskExecutionInfoCache.containsKey(taskInstanceId)) {
			Map<String, String> taskExecutionInfo = taskExecutionInfoCache.get(taskInstanceId);
			taskExecutionInfo.put(key, value);
			logger.log(Level.FINE, "Update task execution info cache of task instance id[{0}], key {1} value {2}", new Object[]{taskInstanceId, key, value});
		} else {
			Map<String, String> taskExecutionInfo = new HashMap<String, String>();
			taskExecutionInfo.put(key, value);
			taskExecutionInfoCache.put(taskInstanceId, taskExecutionInfo);
			logger.log(Level.FINE, "Create task execution info cache of task instance id[{0}], key {1} value {2}", new Object[]{taskInstanceId, key, value});
		}
	}

	public String getFromCache(String taskInstanceId, TaskInformations cacheInfo) {

        // FIXME: Using concurrent package class to prevent thread-safe
        if (taskInstanceInfoCache.get(taskInstanceId) != null) {
            String cacheHit = taskInstanceInfoCache.get(taskInstanceId)[cacheInfo.ordinal()];
            logger.log(Level.FINE, "Hit cache of task instance id[{0}], return {1} as {2}", new Object[]{taskInstanceId, cacheHit, cacheInfo.name()});
            return cacheHit;
        }

        Task task = null;
        try {
            task = activitiAccessor.getTaskService().createTaskQuery().taskId(taskInstanceId).singleResult();
        } catch (ActivitiException e) {
            throw new ProcessException("Can't get task instance by giving ID" + taskInstanceId, e);
        }

        synchronized (this) {
            // Have cached in another thread already?
            // FIXME: Using concurrent package class to prevent thread-safe
            if (taskInstanceInfoCache.get(taskInstanceId) != null) {
                String cacheHit = taskInstanceInfoCache.get(taskInstanceId)[cacheInfo.ordinal()];
                logger.log(Level.FINE, "Hit cache of task instance id[{0}], return {1} as {2}", new Object[]{taskInstanceId, cacheHit, cacheInfo.name()});
                return cacheHit;
            }

            String[] taskRelatedInfo = new String[TaskInformations.values().length];
            taskRelatedInfo[0] = task.getProcessInstanceId();
            String activityDefineId = null;
            activityDefineId = task.getTaskDefinitionKey();
            taskRelatedInfo[1] = activityDefineId;
            
            final Map<String, String> extendAttrs = new HashMap<String, String>();
            final List<String> tdDefines = new ArrayList<String>(4);
            final TaskEntity taskEntity = (TaskEntity) task;
            activitiAccessor.runExtraCommand(new Command<List<String>>() {

                public List<String> execute(CommandContext commandContext) {
                    TaskDefinition td = taskEntity.getTaskDefinition();
                    // FIXME: Support single role definition temporarily
                    tdDefines.add(td.getCandidateGroupIdExpressions().iterator().next().getExpressionText());
                    if (td.getTaskListeners() != null && td.getTaskListeners().size() > 0) {
                        for (List<TaskListener> value : td.getTaskListeners().values()) {
                            if (value != null && !value.isEmpty()) {
                                for (TaskListener tl : value) {
                                    if (ExtendAttrsClassDelegateAdapter.class.isInstance(tl)) {
                                    	// FIXME: Support single extend attributes holder temporarily
                                    	String tlis = ((ExtendAttrsClassDelegateAdapter) tl).getExtendAttrs().get(ActivitiAccessor.TASK_LIFECYCLE_INTERCEPTOR);
                                        tdDefines.add(tlis);
                                        String taskServiceInvokeExpression = ((ExtendAttrsClassDelegateAdapter) tl).getExtendAttrs().get(ActivitiAccessor.TASK_SERVICE_INVOKE_EXPRESSION);
                                        tdDefines.add(taskServiceInvokeExpression);
                                        
                                        // Put into extend attributes map
                                        extendAttrs.putAll(((ExtendAttrsClassDelegateAdapter) tl).getExtendAttrs());
                                    }
                                }
                            }
                        }
                    }

                    return tdDefines;
                }
            });

            taskRelatedInfo[2] = tdDefines.get(0);
            // Adapt call activity/sub-process/two combination case
            String rootProcessInstanceId = activitiAccessor.obtainRootProcess(task.getProcessInstanceId(), true);
            ProcessInstance rootPi = activitiAccessor.getRuntimeService().createProcessInstanceQuery().processInstanceId(rootProcessInstanceId).singleResult();
            Assert.notNull(rootPi.getBusinessKey(), "Business key must not be null, so this means we need upgrade this code to fix it.");
            taskRelatedInfo[3] = rootPi.getBusinessKey();

            // Task extend-attributes
            taskRelatedInfo[4] = tdDefines.size() > 1 ? tdDefines.get(1) : null;
            /* Task operation intercepor place holder */
            taskRelatedInfo[5] = null;

            TaskFormData formData = activitiAccessor.getFormService().getTaskFormData(taskInstanceId);
            taskRelatedInfo[6] = formData.getFormKey();

            // Task service invoke expression
            taskRelatedInfo[7] = tdDefines.size() > 2 ? tdDefines.get(2) : null;
            
            // Task extend attribute
            String extendAttrsXml = XStreamSerializeHelper.serializeXml("extendAttrs", extendAttrs);
            taskRelatedInfo[8] = extendAttrsXml;
            
            // Task define name
            taskRelatedInfo[9] = taskEntity.getName();
            
            // Root process instance ID
            taskRelatedInfo[10] = rootProcessInstanceId;

            // Put into cache
            taskInstanceInfoCache.put(taskInstanceId, taskRelatedInfo);
            logger.log(Level.FINE, "Cache informations of task instance id[{0}].{1}", new Object[]{taskInstanceId, ObjectUtils.getDisplayString(taskRelatedInfo)});
        }

        return taskInstanceInfoCache.get(taskInstanceId)[cacheInfo.ordinal()];
    }
}
