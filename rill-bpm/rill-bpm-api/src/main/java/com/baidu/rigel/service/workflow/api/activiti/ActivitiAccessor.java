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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.ServiceImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
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

import com.baidu.rigel.service.workflow.api.ProcessCreateInteceptor;
import com.baidu.rigel.service.workflow.api.ProcessInstanceEndEvent;
import com.baidu.rigel.service.workflow.api.ProcessOperationInteceptor;
import com.baidu.rigel.service.workflow.api.TaskLifecycleInteceptor;
import com.baidu.rigel.service.workflow.api.TLIGenerator;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import org.activiti.engine.FormService;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

/**
 * Common funciton provider for activiti implementation such as cache, interceptor getter from model.
 * 
 * @author mengran
 */
public class ActivitiAccessor implements InitializingBean, BeanFactoryAware, ApplicationEventPublisherAware {

    public static final String TASK_LIFECYCLE_INTERCEPTOR = "task_lifecycle_interceptor";
    public static final String TASK_FORM_KEY = "url";
    public static final String TASK_ROLE_TAG = "task_role_tag";
    public static final String TASK_SERVICE_INVOKE_EXPRESSION = "taskServiceInvokeExpression";
    
    public static final String TASK_LIFECYCLE_INTERCEPTOR_DELIM = " ";
    
    // Business specify task define ID
    public static final String TASK_DEFINE_ID = "__task_define_id__";
    
    enum TaskInformations {

        PROCESS_INSTANCE_ID, TASK_TAG, TASK_ROLE_TAG, BUSINESS_OBJECT_ID, CLASSDELEGATE_ADAPTER_TLI, CLASSDELEGATE_ADAPTER_TOI, FORM_KEY, TASK_SERVICE_INVOKE_EXPRESSION
    }
    
    /** Logger available to subclasses */
    protected final Logger logger = Logger.getLogger(getClass().getName());

    private Map<String, String[]> taskInstanceInfoCache = new HashMap<String, String[]>();
    private BeanFactory beanFactory;
    private ApplicationEventPublisher applicationEventPublisher;
    private RuntimeService runtimeService;
    private TaskService taskService;
    private RepositoryService repositoryService;
    private IdentityService identityService;
    private FormService formService;
    private List<TaskLifecycleInteceptor> commonTaskLifecycleInterceptor;
    private List<ProcessCreateInteceptor> processCreateInteceptor;
    private List<ProcessOperationInteceptor> processOperationInteceptors;
    private ActivitiExtraService extraService;
    private ProcessEngine processEngine;
    private ProcessEngineConfiguration processEngineConfiguration;
    private boolean rigelWfInitialize = false;

    public boolean isRigelWfInitialize() {
        return rigelWfInitialize;
    }

    public void setRigelWfInitialize(boolean rigelWfInitialize) {
        this.rigelWfInitialize = rigelWfInitialize;
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
     * @return the processCreateInteceptor
     */
    public List<ProcessCreateInteceptor> getProcessCreateInteceptor() {
        return processCreateInteceptor;
    }

    /**
     * @param processCreateInteceptor the processCreateInteceptor to set
     */
    public void setProcessCreateInteceptor(
            List<ProcessCreateInteceptor> processCreateInteceptor) {
        this.processCreateInteceptor = processCreateInteceptor;
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
     * @return the beanFactory
     */
    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    /**
     * @return the commonTaskLifecycleInterceptor
     */
    public List<TaskLifecycleInteceptor> getCommonTaskLifecycleInterceptor() {
        return commonTaskLifecycleInterceptor;
    }

    /**
     * @param commonTaskLifecycleInterceptor the commonTaskLifecycleInterceptor to set
     */
    public void setCommonTaskLifecycleInterceptor(
            List<TaskLifecycleInteceptor> commonTaskLifecycleInterceptor) {
        this.commonTaskLifecycleInterceptor = commonTaskLifecycleInterceptor;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {

        this.beanFactory = beanFactory;
    }

    /**
     * @param applicationEventPublisher the applicationEventPublisher to set
     */
    public void setApplicationEventPublisher(
            ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * @return the applicationEventPublisher
     */
    public ApplicationEventPublisher getApplicationEventPublisher() {
        return applicationEventPublisher;
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

        if (this.getProcessEngine() == null) {
            Assert.notNull(this.getProcessEngineConfiguration(), "Properties 'ProcessEngineConfiguration' is required.");

            // Retrieve process engine from it's holder
            this.setProcessEngine(ProcessEngines.getProcessEngine(this.getProcessEngineConfiguration().getProcessEngineName()));
            if (getProcessEngine() != null) {
                logger.log(Level.INFO, "Retrive process engine from it''s holder.{0}", getProcessEngine());
            } else {
                // Build process engine
                this.setProcessEngine(getProcessEngineConfiguration().buildProcessEngine());
                logger.log(Level.INFO, "Build process engine from it''s configuration.{0}", getProcessEngine());
            }
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

        // Generate application context as event publisher
        if (!(getBeanFactory() instanceof ApplicationEventPublisher)) {
            // Means aware method have not been call.
            // FIXME: Adapt spring v2.0 implemetation, we use new SimpleApplicationEventMulticaster() but not new SimpleApplicationEventMulticaster(BeanFactory)
            logger.log(Level.INFO, "Adapt external environment[Not ApplicationContext].{0}", getBeanFactory());
            this.applicationEventPublisher = new ApplicationEventPublisherAdapter(new SimpleApplicationEventMulticaster());
        }

        // RIGEL_WF_* data-base initialize
        if (!rigelWfInitialize) {
            rigelWfInitialize = runExtraCommand(new Command<Boolean>() {

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
    }

    private class ActivitiExtraService extends ServiceImpl {

        public <T> T doOperation(Command<T> command) {

            logger.log(Level.INFO, "Run extra command[{0}].", command);
            return getCommandExecutor().execute(command);
        }
    }

    private class ApplicationEventPublisherAdapter implements ApplicationEventPublisher {

        private SimpleApplicationEventMulticaster saemc;

        public ApplicationEventPublisherAdapter(SimpleApplicationEventMulticaster aemc) {
            this.saemc = aemc;
//            this.saemc.setBeanFactory(getBeanFactory());
            String[] listenerNames = ((ListableBeanFactory) getBeanFactory()).getBeanNamesForType(ApplicationListener.class);
            if (listenerNames != null && listenerNames.length > 0) {
                for (String listenerName : listenerNames) {
                    logger.log(Level.INFO, "Add application listener named [{0}].", listenerName);
                    aemc.addApplicationListener((ApplicationListener) getBeanFactory().getBean(listenerName));
                }
            }
        }

        public void publishEvent(ApplicationEvent event) {

            // Delegate operatoin
            this.saemc.multicastEvent(event);
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
    private String obtainCacheInfos(String taskInstanceId, TaskInformations cacheInfo) {

        Assert.hasText(taskInstanceId, "taskInstanceId pass in must not empty");
        Assert.notNull(cacheInfo, "taskInformations must not null");

        String cacheHit = null;
//        // For work fine, we first find from external cache.
//        if (getExternalWorkflowCache() != null) {
//            try {
//                cacheHit = getExternalWorkflowCache().getTaskInfos(taskInstanceId, cacheInfo);
//            } catch(Throwable t) {
//                logger.log(Level.WARNING, "External cache provider:" + getExternalWorkflowCache().getClass().getName() + " throw a exception.", t);
//            }
//        }
        if (cacheHit == null) {
            logger.log(Level.FINE, "Can not hit external cache of task instance id:{0}, cache info:{1}, and try to get cache info from native cache.", new Object[]{taskInstanceId, cacheInfo});
            cacheHit = peerJVMCache(taskInstanceId, cacheInfo);
        } else {
            logger.log(Level.FINE, "Hit external cache of task instance id:{0}, cache info:{1}", new Object[]{taskInstanceId, cacheInfo});
        }

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

            final List<String> tdDefines = new ArrayList<String>(4);
            ProcessInstance pi = getRuntimeService().createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
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
                                    if (ClassDelegateAdapter.class.isInstance(tl)) {
                                        tdDefines.add(StringUtils.collectionToDelimitedString(((ClassDelegateAdapter) tl).obtainTaskLifycycleInterceptors(), TASK_LIFECYCLE_INTERCEPTOR_DELIM));
                                        tdDefines.add(StringUtils.collectionToDelimitedString(((ClassDelegateAdapter) tl).getTaskServiceInvokeExpression(), TASK_LIFECYCLE_INTERCEPTOR_DELIM));
                                    }
                                }
                            }
                        }
                    }

                    return tdDefines;
                }
            });

            taskRelatedInfo[2] = tdDefines.get(0);
            taskRelatedInfo[3] = pi.getBusinessKey();

            // Task extend-attributes
            taskRelatedInfo[4] = tdDefines.size() > 1 ? tdDefines.get(1) : null;
            /* Task operation intercepor place holder */
            taskRelatedInfo[5] = null;

            TaskFormData formData = getFormService().getTaskFormData(taskInstanceId);
            taskRelatedInfo[6] = formData.getFormKey();

            // Task service invoke expression
            taskRelatedInfo[7] = tdDefines.size() > 2 ? tdDefines.get(2) : null;

            // Put into cache
            taskInstanceInfoCache.put(taskInstanceId, taskRelatedInfo);
            logger.log(Level.FINE, "Cache informations of task instance id[{0}].{1}", new Object[]{taskInstanceId, ObjectUtils.getDisplayString(taskRelatedInfo)});
        }

        return taskInstanceInfoCache.get(taskInstanceId)[cacheInfo.ordinal()];
    }

    /**
     * 子类可以扩展这个方法，因为有可能业务系统已经持久化了这个关系
     * @param taskInstanceId 任务ID
     * @return 流程ID
     */
    protected String obtainProcessInstanceId(String taskInstanceId) {

        return obtainCacheInfos(taskInstanceId, TaskInformations.PROCESS_INSTANCE_ID);
    }

    /**
     * 子类可以扩展这个方法，因为有可能业务系统已经持久化了这个关系
     * @param taskInstanceId 任务ID
     * @return 流程ID
     */
    protected String obtainTaskTag(String taskInstanceId) {

        return obtainCacheInfos(taskInstanceId, TaskInformations.TASK_TAG);
    }

    /**
     * 子类可以扩展这个方法，因为有可能业务系统已经持久化了这个关系
     * @param taskInstanceId 任务ID
     * @return 流程ID
     */
    protected String obtainTaskRoleTag(String taskInstanceId) {

        return obtainCacheInfos(taskInstanceId, TaskInformations.TASK_ROLE_TAG);
    }

    /**
     * 子类可以扩展这个方法，因为有可能业务系统已经持久化了这个关系
     * @param taskInstanceId 任务ID
     * @return 业务对象ID
     */
    protected String obtainBusinessObjectId(String taskInstanceId) {

        // FIXME: Business object may contain in GWFP engine, current version set it into otherInfos
        return obtainCacheInfos(taskInstanceId, TaskInformations.BUSINESS_OBJECT_ID);
    }

    protected HashMap<String, String> getExtendAttrs(String taskInstanceId) {

        try {
//            Task task = getTaskService().createTaskQuery().taskId(taskInstanceId).singleResult();
//            String document = task.getDescription();

            Map<String, String> extendAttrsMap = new HashMap<String, String>();
            // Retrieve from TLITOI holder
            String tli = obtainCacheInfos(taskInstanceId, TaskInformations.CLASSDELEGATE_ADAPTER_TLI);
            logger.log(Level.FINEST, "Retrieve from TLI holder--Task[{0}] :{1}", new Object[]{taskInstanceId, ObjectUtils.getDisplayString(tli)});
            String formKey = obtainCacheInfos(taskInstanceId, TaskInformations.FORM_KEY);
            logger.log(Level.FINEST, "Retrieve from formKey holder--Task[{0}] :{1}", new Object[]{taskInstanceId, ObjectUtils.getDisplayString(formKey)});
            String taskRoleTag = obtainCacheInfos(taskInstanceId, TaskInformations.TASK_ROLE_TAG);
            logger.log(Level.FINEST, "Retrieve from taskRoleTag holder--Task[{0}] :{1}", new Object[]{taskInstanceId, ObjectUtils.getDisplayString(taskRoleTag)});
            String taskServiceInvoikeExpression = obtainCacheInfos(taskInstanceId, TaskInformations.TASK_SERVICE_INVOKE_EXPRESSION);
            logger.log(Level.FINEST, "Retrieve from taskServiceInvoikeExpression holder--Task[{0}] :{1}", new Object[]{taskInstanceId, ObjectUtils.getDisplayString(taskServiceInvoikeExpression)});
                        
            if (StringUtils.hasLength(tli)) {
                extendAttrsMap.put(TASK_LIFECYCLE_INTERCEPTOR, tli);
            }
            if (StringUtils.hasLength(formKey)) {
                extendAttrsMap.put(TASK_FORM_KEY, formKey.trim());
            }
            if (StringUtils.hasLength(taskRoleTag)) {
                extendAttrsMap.put(TASK_ROLE_TAG, taskRoleTag.trim());
            }
            if (StringUtils.hasLength(taskServiceInvoikeExpression)) {
                extendAttrsMap.put(TASK_SERVICE_INVOKE_EXPRESSION, taskServiceInvoikeExpression.trim());
            }
//            if (!StringUtils.hasText(document)) {
//                logger.finest("PARSING EXTEND ATTRS--Task[" + taskInstanceId + "] description is empty.");
//                return extendAttrsMap.isEmpty() ? null : extendAttrsMap;
//            }
//
//            logger.finest("PARSING EXTEND ATTRS--Task[" + taskInstanceId + "] description:" + document);
//
//            String[] arrayString = StringUtils.tokenizeToStringArray(document, "\r\n");
            HashMap<String, String> forReturn = new HashMap<String, String>();
//            for (String element : arrayString) {
//                forReturn.put(StringUtils.split(element, ":")[0], StringUtils.split(element, ":")[1]);
//            }

            forReturn.putAll(extendAttrsMap);
            logger.log(Level.FINE, "PARSING EXTEND ATTRS--Task[{0}] description/TLITOI holder result:{1}", new Object[]{taskInstanceId, ObjectUtils.getDisplayString(forReturn)});
            return forReturn;

        } catch (ActivitiException e) {
            throw new ProcessException("Can not obtain task[" + taskInstanceId + "] extension attribute", e);
        }

    }

    protected final String[] obtainCommaSplitSpecifyValues(String taskInstanceId, String extendsAttributeKey, String delim) {

        Map<String, String> adArray = getExtendAttrs(taskInstanceId);
        if (adArray == null || adArray.isEmpty()) {
            logger.fine("Return empty array because extend attribute is empty.");
            return new String[0];
        }

        Set<String> valueSet = new LinkedHashSet<String>();
        for (Entry<String, String> entry : adArray.entrySet()) {
            if (extendsAttributeKey.equals(entry.getKey())) {
                logger.log(Level.FINEST, "Match entry for key[{0}], value[{1}].", new Object[]{extendsAttributeKey, entry.getValue()});
                String[] interceptorData = StringUtils.delimitedListToStringArray(StringUtils.trimAllWhitespace(entry.getValue()), delim);
                if (interceptorData != null && interceptorData.length > 0) {
                    valueSet.addAll(Arrays.asList(interceptorData));
                }
            }
        }

        logger.log(Level.FINE, "PARSING EXTEND ATTRS--Task[{0}] extension attribute key[{1}]:{2}", new Object[]{taskInstanceId, extendsAttributeKey, ObjectUtils.getDisplayString(valueSet)});
        return valueSet.toArray(new String[valueSet.size()]);
    }
    
    private final TLIGenerator<TaskLifecycleInteceptor> DEFAULT_SPRING_TLI_GENERATOR = new SpringBeanTLIGenerator();
    
    private class SpringBeanTLIGenerator extends SpringBeanGenerator<TaskLifecycleInteceptor> {
        
    }
    
    private abstract class SpringBeanGenerator<T> implements TLIGenerator<T> {

        protected Class<T> actualClazz;

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
                logger.log(Level.FINE, "{0}#supportGeneratePattern exception, ignore it.", SpringBeanGenerator.class.getName());
                result = false;
            }

            return result;
        }

        @SuppressWarnings("unchecked")
        private <T> Collection<T> convertNameToBean(Class<T> clazz, String[] beanName) {

            if (ObjectUtils.isEmpty(beanName)) {
                logger.fine("Return empty array because bean names is empty.");
                return new ArrayList<T>(0);
            }

            LinkedHashSet<T> beanList = new LinkedHashSet<T>(beanName.length);
            for (String name : beanName) {
                String[] interceptorData = StringUtils.commaDelimitedListToStringArray(StringUtils.trimAllWhitespace(name));
                for (String interceptor: interceptorData) {
                    Assert.isTrue(beanFactory.containsBean(interceptor), "Bean name[" + interceptor + "] may not be registed in Spring bean factory.");
                    logger.log(Level.FINEST, "Find bean named[{0}] and add it for return.", interceptor);
                    beanList.add((T) beanFactory.getBean(interceptor));
                }
            }

            logger.log(Level.FINE, "Convert bean names[{0}] to beans {1}", new Object[]{ObjectUtils.getDisplayString(beanName), ObjectUtils.getDisplayString(beanList)});
            return beanList;
        }
    }

    protected final TaskLifecycleInteceptor[] obtainTaskLifecycleInterceptors(String taskInstanceId) {

        Collection<TaskLifecycleInteceptor> taskLifecycleInterceptors = new LinkedHashSet();
        
        // Add commen TLI configuration
        if (this.commonTaskLifecycleInterceptor != null && !this.commonTaskLifecycleInterceptor.isEmpty()) {
            logger.log(Level.FINEST, "Combin common task-lifecycle-interceptor[{0}].", ObjectUtils.getDisplayString(this.commonTaskLifecycleInterceptor));
            taskLifecycleInterceptors.addAll(commonTaskLifecycleInterceptor);
        }
        
        String[] perTaskInterceptorConfigs = obtainCommaSplitSpecifyValues(taskInstanceId, TASK_LIFECYCLE_INTERCEPTOR, TASK_LIFECYCLE_INTERCEPTOR_DELIM);
        List<TLIGenerator> tempGeneratorList = new ArrayList<TLIGenerator>(1);
        tempGeneratorList.add(DEFAULT_SPRING_TLI_GENERATOR);
        for (TLIGenerator generator : tempGeneratorList) {
            if (generator.supportGeneratePattern(perTaskInterceptorConfigs)) {
                taskLifecycleInterceptors.addAll(generator.generate(perTaskInterceptorConfigs));
            }
        }

        logger.log(Level.FINE, "Return task[{0}] task-lifecycle-interceptor:{1}", new Object[]{taskInstanceId, ObjectUtils.getDisplayString(taskLifecycleInterceptors)});
        return taskLifecycleInterceptors.toArray(new TaskLifecycleInteceptor[taskLifecycleInterceptors.size()]);
    }

    protected final boolean hasParentProcess(String processInstanceId) {

        // Delegate this operation
        // != change to !equals by MENGRAN at 2011-01-05
        return !obtainRootProcess(processInstanceId).equals(processInstanceId);
    }

    protected final String obtainRootProcess(String processInstanceId) {

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

    protected void publishProcessEndEvent(String processInstanceId, String triggerTaskInstanceId, ActivitiTaskExecutionContext triggerTaskExecutionContext) {

        logger.log(Level.INFO, "Process instance[{0}] end. Trigger task[{1}]", new Object[]{processInstanceId, triggerTaskInstanceId});
        this.applicationEventPublisher.publishEvent(new ProcessInstanceEndEvent(processInstanceId,
                triggerTaskInstanceId, hasParentProcess(processInstanceId), triggerTaskExecutionContext));
    }
}
