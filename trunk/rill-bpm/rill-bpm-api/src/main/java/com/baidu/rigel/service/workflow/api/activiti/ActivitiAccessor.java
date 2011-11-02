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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.FormService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.ServiceImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.baidu.rigel.service.workflow.api.ProcessOperationInteceptor;
import com.baidu.rigel.service.workflow.api.WorkflowTemplate;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;

/**
 * Activiti engine access helper class
 * 
 * @author mengran
 */
public abstract class ActivitiAccessor extends WorkflowTemplate implements InitializingBean, BeanFactoryAware, ApplicationEventPublisherAware {

    private Map<String, String[]> taskInstanceInfoCache = new HashMap<String, String[]>();
    private RuntimeService runtimeService;
    private TaskService taskService;
    private RepositoryService repositoryService;
    private IdentityService identityService;
    private FormService formService;
    private List<ProcessOperationInteceptor> processOperationInteceptors;
    private ActivitiExtraService extraService;
    private ProcessEngine processEngine;
    private ProcessEngineConfiguration processEngineConfiguration;

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
     * @return the taskInstanceInfoCache
     */
    public final Map<String, String[]> getTaskInstanceInfoCache() {
        return taskInstanceInfoCache;
    }

    /**
     * @param taskInstanceInfoCache the taskInstanceInfoCache to set
     */
    public final void setTaskInstanceInfoCache(
            Map<String, String[]> taskInstanceInfoCache) {
        this.taskInstanceInfoCache = taskInstanceInfoCache;
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

            // Retrieve process engine from it's holder
            this.setProcessEngine(getProcessEngineConfiguration().buildProcessEngine());
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

        // Initialize extra service
        this.extraService = new ActivitiExtraService();
        BeanUtils.copyProperties(this.getRuntimeService(), this.extraService);

        // RIGEL_WF_* DB initialize
        runExtraCommand(new Command<Boolean>() {

            public Boolean execute(CommandContext commandContext) {

                boolean tablePresent = commandContext.getDbSqlSession().isTablePresent("RIGEL_WF_TRANSITION_TAKE_TRACE");
                if (tablePresent) {
                    return true;
                }

                // Do create
                String resourceName = getResourceForDbOperation(commandContext.getDbSqlSession(), "create", "create", "wf");
                commandContext.getDbSqlSession().executeSchemaResource("create", "wf", resourceName, false);
                return true;
            }

            String getResourceForDbOperation(DbSqlSession dbSqlSession, String directory, String operation, String component) {
                String databaseType = dbSqlSession.getDbSqlSessionFactory().getDatabaseType();
                return "com/baidu/rigel/service/workflow/db/" + directory + "/rigel." + databaseType + "." + operation + "." + component + ".sql";
            }
        });
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
        logger.log(Level.FINE, "Can not hit external cache of task instance id:{0}, cache info:{1}, and try to get cache info from native cache.", new Object[]{taskInstanceId, cacheInfo});
        cacheHit = peerJVMCache(taskInstanceId, cacheInfo);

        return cacheHit;
    }

    private String peerJVMCache(String taskInstanceId, TaskInformations cacheInfo) {

        // FIXME: Using concurrent package class to prevent thread-safe
        if (taskInstanceInfoCache.get(taskInstanceId) != null) {
            String cacheHit = taskInstanceInfoCache.get(taskInstanceId)[cacheInfo.ordinal()];
            logger.log(Level.FINE, "Hit cache of task instance id[{0}], return {1} as {2}", new Object[]{taskInstanceId, cacheHit, cacheInfo.name()});
            return cacheHit;
        }

        Task task = null;
        try {
            task = getTaskService().createTaskQuery().taskId(taskInstanceId).singleResult();
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
            this.extraService.doOperation(new Command<List<String>>() {

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
                                    	String tlis = ((ExtendAttrsClassDelegateAdapter) tl).getExtendAttrs().get(TASK_LIFECYCLE_INTERCEPTOR);
                                        tdDefines.add(tlis);
                                        String taskServiceInvokeExpression = ((ExtendAttrsClassDelegateAdapter) tl).getExtendAttrs().get(TASK_SERVICE_INVOKE_EXPRESSION);
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
            String rootProcessInstanceId = obtainRootProcess(task.getProcessInstanceId(), true);
            ProcessInstance rootPi = getRuntimeService().createProcessInstanceQuery().processInstanceId(rootProcessInstanceId).singleResult();
            Assert.notNull(rootPi.getBusinessKey(), "Business key must not be null, so this means we need upgrade this code to fix it.");
            taskRelatedInfo[3] = rootPi.getBusinessKey();

            // Task extend-attributes
            taskRelatedInfo[4] = tdDefines.size() > 1 ? tdDefines.get(1) : null;
            /* Task operation intercepor place holder */
            taskRelatedInfo[5] = null;

            TaskFormData formData = getFormService().getTaskFormData(taskInstanceId);
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
