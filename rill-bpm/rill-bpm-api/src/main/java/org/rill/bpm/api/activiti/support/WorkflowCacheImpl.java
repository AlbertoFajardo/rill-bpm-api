package org.rill.bpm.api.activiti.support;

import java.util.List;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.rill.bpm.api.WorkflowCache;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.WorkflowOperations.XStreamSerializeHelper;
import org.rill.bpm.api.activiti.ActivitiAccessor;
import org.rill.bpm.api.activiti.ExtendAttrsClassDelegateAdapter;
import org.rill.bpm.api.exception.ProcessException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.Assert;


public class WorkflowCacheImpl implements WorkflowCache {

	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	private WorkflowOperations workflowAccessor;
	
	public final WorkflowOperations getWorkflowAccessor() {
		return workflowAccessor;
	}
	
	@Required
	public final void setWorkflowAccessor(WorkflowOperations workflowAccessor) {
		this.workflowAccessor = workflowAccessor;
	}

	@Override
	@Cacheable(value = { "default" })
	public String getTaskRelatedInfo(String taskInstanceId, String taskInformationsKey) {
		
		logger.info("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + taskInformationsKey);
		WorkflowOperations.TaskInformations key = WorkflowOperations.TaskInformations.valueOf(taskInformationsKey);
		
		ActivitiAccessor activitiAccessor = ActivitiAccessor.retrieveActivitiAccessorImpl(workflowAccessor, ActivitiAccessor.class);
		TaskEntity task = null;
        try {
            task = (TaskEntity) activitiAccessor.getTaskService().createTaskQuery().taskId(taskInstanceId).singleResult();
        } catch (ActivitiException e) {
            throw new ProcessException("Can't get task instance by giving ID" + taskInstanceId, e);
        }
        final TaskEntity taskEntity = task;

        if (WorkflowOperations.TaskInformations.PROCESS_INSTANCE_ID.equals(key)) {
        	logger.info("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + task.getProcessInstanceId());
        	return task.getProcessInstanceId();
        }
        if (WorkflowOperations.TaskInformations.TASK_TAG.equals(key)) {
        	logger.info("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + task.getTaskDefinitionKey());
        	return task.getTaskDefinitionKey();
        }
        if (WorkflowOperations.TaskInformations.TASK_ROLE_TAG.equals(key)) {
        	String taskRoleTag = activitiAccessor.runExtraCommand(new Command<String>() {

				@Override
				public String execute(CommandContext commandContext) {
					TaskDefinition td = taskEntity.getTaskDefinition();
					return td.getCandidateGroupIdExpressions().iterator().next().getExpressionText();
				}
        		
            });
        	logger.info("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + taskRoleTag);
        	return taskRoleTag;
        }
        if (WorkflowOperations.TaskInformations.BUSINESS_OBJECT_ID.equals(key)) {
        	// Adapt call activity/sub-process/two combination case
            String rootProcessInstanceId = activitiAccessor.obtainRootProcess(task.getProcessInstanceId(), true);
            ProcessInstance rootPi = activitiAccessor.getRuntimeService().createProcessInstanceQuery().processInstanceId(rootProcessInstanceId).singleResult();
            Assert.notNull(rootPi.getBusinessKey(), "Business key must not be null, so this means we need upgrade this code to fix it.");
            logger.info("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + rootPi.getBusinessKey());
            return rootPi.getBusinessKey();
        }
        if (WorkflowOperations.TaskInformations.CLASSDELEGATE_ADAPTER_TLI.equals(key)) {
        	String classDelegateTli = activitiAccessor.runExtraCommand(new Command<String>() {

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
                                        return tlis;
                                    }
                                }
                            }
                        }
                    }
					// Not found
					return null;
				}
        		
            });
        	logger.info("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + classDelegateTli);
        }
        
        if (WorkflowOperations.TaskInformations.CLASSDELEGATE_ADAPTER_TOI.equals(key)) {
        	logger.info("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + "null");
        	return null;
        }
        
        if (WorkflowOperations.TaskInformations.FORM_KEY.equals(key)) {
        	
        	String formKey = activitiAccessor.getFormService().getTaskFormData(taskInstanceId).getFormKey();
        	logger.info("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + formKey);
        }
        
        if (WorkflowOperations.TaskInformations.TASK_SERVICE_INVOKE_EXPRESSION.equals(key)) {
        	String taskServiceInvokeExp = activitiAccessor.runExtraCommand(new Command<String>() {

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
                                        return values;
                                    }
                                }
                            }
                        }
                    }
					// Not found
					return null;
				}
        		
            });
        	logger.info("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + taskServiceInvokeExp);
        }
        
        if (WorkflowOperations.TaskInformations.EXTEND_ATTRIBUTES.equals(key)) {
        	String extendAttributes = activitiAccessor.runExtraCommand(new Command<String>() {

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
                                        return values;
                                    }
                                }
                            }
                        }
                    }
					// Not found
					return null;
				}
        		
            });
        	logger.info("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + extendAttributes);
        }
        
        if (WorkflowOperations.TaskInformations.TASK_DEFINE_NAME.equals(key)) {
        	logger.info("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + task.getName());
        	return task.getName();
        }
        
        if (WorkflowOperations.TaskInformations.ROOT_PROCESS_INSTANCE_ID.equals(key)) {
        	String rootProcessInstanceId = activitiAccessor.obtainRootProcess(task.getProcessInstanceId(), true);
        	logger.info("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + rootProcessInstanceId);
        	return rootProcessInstanceId;
        }
        
        if (WorkflowOperations.TaskInformations.PROCESS_DEFINE_KEY.equals(key)) {
        	String processDefinitionKey = activitiAccessor.runExtraCommand(new Command<String>() {

				@Override
				public String execute(CommandContext commandContext) {
					ProcessDefinitionEntity pde = commandContext.getProcessDefinitionManager().findLatestProcessDefinitionById(taskEntity.getProcessDefinitionId());
					return pde.getKey();
				}
        		
            });
        	logger.info("NOT HIT: method[getTaskRelatedInfo] cache key:" + taskInstanceId + "," + key + "; value:" + processDefinitionKey);
        }
        
        throw new UnsupportedOperationException("Unsupported key: " + key);
	}

	@Override
	@Cacheable(value = { "default" }, key="#key")
	public String getOrSetUserInfo(String key, String value) {
		
		logger.info("NOT HIT: method[getOrSetUserInfo] cache key:" + key + ", value:" + value);
		return value;
	}

}
