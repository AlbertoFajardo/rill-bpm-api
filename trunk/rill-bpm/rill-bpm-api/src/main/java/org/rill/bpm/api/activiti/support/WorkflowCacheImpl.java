package org.rill.bpm.api.activiti.support;

import java.util.List;

import javax.annotation.Resource;

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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.Assert;


public class WorkflowCacheImpl implements WorkflowCache {

	@Resource
	private WorkflowOperations workflowAccessor;
	
	@Override
	@Cacheable(value = { "default" })
	public String getTaskRelatedInfo(String taskInstanceId, WorkflowOperations.TaskInformations key) {
		
		ActivitiAccessor activitiAccessor = ActivitiAccessor.retrieveActivitiAccessorImpl(workflowAccessor, ActivitiAccessor.class);
		TaskEntity task = null;
        try {
            task = (TaskEntity) activitiAccessor.getTaskService().createTaskQuery().taskId(taskInstanceId).singleResult();
        } catch (ActivitiException e) {
            throw new ProcessException("Can't get task instance by giving ID" + taskInstanceId, e);
        }
        final TaskEntity taskEntity = task;

        if (WorkflowOperations.TaskInformations.PROCESS_INSTANCE_ID.equals(key)) {
        	return task.getProcessInstanceId();
        }
        if (WorkflowOperations.TaskInformations.TASK_TAG.equals(key)) {
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
        	return taskRoleTag;
        }
        if (WorkflowOperations.TaskInformations.BUSINESS_OBJECT_ID.equals(key)) {
        	// Adapt call activity/sub-process/two combination case
            String rootProcessInstanceId = activitiAccessor.obtainRootProcess(task.getProcessInstanceId(), true);
            ProcessInstance rootPi = activitiAccessor.getRuntimeService().createProcessInstanceQuery().processInstanceId(rootProcessInstanceId).singleResult();
            Assert.notNull(rootPi.getBusinessKey(), "Business key must not be null, so this means we need upgrade this code to fix it.");
            return rootPi.getBusinessKey();
        }
        if (WorkflowOperations.TaskInformations.CLASSDELEGATE_ADAPTER_TLI.equals(key)) {
        	return activitiAccessor.runExtraCommand(new Command<String>() {

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
        }
        
        if (WorkflowOperations.TaskInformations.CLASSDELEGATE_ADAPTER_TOI.equals(key)) {
        	return null;
        }
        
        if (WorkflowOperations.TaskInformations.FORM_KEY.equals(key)) {
        	
        	return activitiAccessor.getFormService().getTaskFormData(taskInstanceId).getFormKey();
        }
        
        if (WorkflowOperations.TaskInformations.TASK_SERVICE_INVOKE_EXPRESSION.equals(key)) {
        	return activitiAccessor.runExtraCommand(new Command<String>() {

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
        }
        
        if (WorkflowOperations.TaskInformations.EXTEND_ATTRIBUTES.equals(key)) {
        	return activitiAccessor.runExtraCommand(new Command<String>() {

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
        }
        
        if (WorkflowOperations.TaskInformations.TASK_DEFINE_NAME.equals(key)) {
        	return task.getName();
        }
        
        if (WorkflowOperations.TaskInformations.ROOT_PROCESS_INSTANCE_ID.equals(key)) {
        	String rootProcessInstanceId = activitiAccessor.obtainRootProcess(task.getProcessInstanceId(), true);
        	return rootProcessInstanceId;
        }
        
        if (WorkflowOperations.TaskInformations.PROCESS_DEFINE_KEY.equals(key)) {
        	return activitiAccessor.runExtraCommand(new Command<String>() {

				@Override
				public String execute(CommandContext commandContext) {
					ProcessDefinitionEntity pde = commandContext.getProcessDefinitionManager().findLatestProcessDefinitionById(taskEntity.getProcessDefinitionId());
					return pde.getKey();
				}
        		
            });
        }
        
        throw new UnsupportedOperationException("Unsupported key: " + key);
	}

	@Override
	@Cacheable(value = { "default" }, key="#key")
	public String getOrSetUserInfo(String key, String value) {
		
		return value;
	}

}
