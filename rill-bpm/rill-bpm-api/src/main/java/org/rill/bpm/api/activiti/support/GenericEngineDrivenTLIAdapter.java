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
package org.rill.bpm.api.activiti.support;

import java.lang.reflect.Method;
import java.util.logging.Level;

import org.activiti.engine.impl.util.ReflectUtil;
import org.rill.bpm.api.TaskExecutionContext;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.activiti.ActivitiAccessor;
import org.rill.bpm.api.exception.ProcessException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;


/**
 * Common engine driven TLI adapter.
 *
 * <p>
 * Obtain task service invoke expression from:
 * <ul>
 *  <li> from {@link org.rill.bpm.api.activiti.ExtendAttrsClassDelegateAdapter}'s configuration.
 *  <li> [taskName]Service.[taskName]Complete(DTO.class.getName()) that follow JavaEE6's [Configuration With Exception], only if {@link #isJavaEE6CWE() FLAG} is true.
 * 
 * @author mengran
 */
public class GenericEngineDrivenTLIAdapter extends EngineDrivenTLIAdapter<Object> implements BeanFactoryAware {

    private static final String UNKNOWN = GenericEngineDrivenTLIAdapter.class.getName() + ".UNKNOWN";

    private WorkflowOperations workflowAccessor;
    private BeanFactory beanFactory;

    private boolean javaEE6CWE = false;

    public boolean isJavaEE6CWE() {
        return javaEE6CWE;
    }

    public void setJavaEE6CWE(boolean javaEE6CWE) {
        this.javaEE6CWE = javaEE6CWE;
    }
    

    public WorkflowOperations getWorkflowAccessor() {
        return workflowAccessor;
    }

    public void setWorkflowAccessor(WorkflowOperations workflowAccessor) {
        this.workflowAccessor = workflowAccessor;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {

        this.beanFactory = beanFactory;
    }

    private String obtainTaskServiceInvokeExpression(TaskExecutionContext taskExecutionContext) {

        return getWorkflowAccessor().getTaskInstanceInformations(taskExecutionContext.getTaskInstanceId()).get(ActivitiAccessor.TASK_SERVICE_INVOKE_EXPRESSION);
    }

    private boolean isArray(Object obj) {

        return obj != null && obj.getClass().isArray();
    }

    @Override
    protected Object doEngineDriven(Object t, TaskExecutionContext taskExecutionContext) {
        
        String expression = null;
        // Try to obtain from configuration.
        expression = obtainTaskServiceInvokeExpression(taskExecutionContext);
        if (!StringUtils.hasLength(expression)) {
            if (!isJavaEE6CWE()) {
                logger.fine("Can not find task service invoke expression from task extend attrs, please set flag to [true] for follow JavaEE6CWE");
                return null;
            }
            // Build follow JavaEE6's [Configuration With Exception].
            expression = taskExecutionContext.getTaskDefineName() + "Service." + taskExecutionContext.getTaskDefineName() + "Complete(" + UNKNOWN + ")";

            logger.log(Level.FINEST,"Can not find task service invoke expression from task extend attrs, " + "and we build one follow JavaEE6''s [Configuration With Exception] pattern : {0}", expression);
        }
        logger.log(Level.FINE, "Using task service invoke expression: {0}", expression);

        // Prepare invoke informations
        String[] beforeLeftBracket = expression.substring(0, expression.indexOf("(")).split("\\.");
        Assert.isTrue(beforeLeftBracket.length == 2, "The expression parttern is [taskName]Service.[taskName]Complete(DTO.class.getName()): " + expression);
        String serviceName = beforeLeftBracket[0].trim();
        String methodName = beforeLeftBracket[1].trim();
        String parameters = expression.substring(expression.indexOf("(") + 1, expression.indexOf(")")).trim();
        String[] params = parameters.split(",");
        Object serviceBean = this.beanFactory.getBean(serviceName);
//        Class<?> clazz = serviceBean instanceof SpringProxy ? ((Advised) serviceBean).getTargetClass() : serviceBean.getClass();
        // Direct return service bean's class, maybe proxied.
        Class<?> clazz = serviceBean.getClass();

        // Do invoke
        if (UNKNOWN.equals(parameters)) {
            // JavaEE6's [Configuration With Exception] pattern.
            throw new UnsupportedOperationException("Support soon...");
        } else if (!StringUtils.hasLength(parameters)) {
            // Empty parameter.
            Method method = ReflectionUtils.findMethod(clazz, methodName);
            // Not empty array or not null
            if ((isArray(t) && !ObjectUtils.isEmpty((Object[]) t)) || t != null) {
                logger.log(Level.WARNING, "Invoke expression: {0}, and is not need any parameter.", expression);
            }
            logger.log(Level.FINE, "Reflect method: {0} on service bean: {1} for expression:{2}", new Object[]{method, serviceBean, expression});
            ReflectionUtils.makeAccessible(method);
            return ReflectionUtils.invokeMethod(method, serviceBean);
        } else {
            // Empty array or null object
            if ((isArray(t) && ObjectUtils.isEmpty((Object[]) t)) || t == null) {
                throw new ProcessException("Task service invoke expression parameter:" + parameters + ", so we need a actual parameter.");
            }
            // Prepare invoke parameter
            Class<?>[] parameterClasses = new Class<?>[params.length];
            Object[] parameterObject = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                parameterClasses[i] = ClassUtils.resolveClassName(params[i].trim(), ClassUtils.getDefaultClassLoader());
                if (ClassUtils.isPrimitiveOrWrapper(parameterClasses[i])) {
                    parameterObject[i] = ((Object[]) t)[i];
                } else {
                    parameterObject[i] = ReflectUtil.instantiate(params[i].trim());
                }
            }
            // Shallow copy
            if (isArray(t)) {
                parameterObject = (Object[]) t;
            } else {
                ReflectionUtils.shallowCopyFieldState(t, parameterObject[0]);
            }
            
            Method method = ReflectionUtils.findMethod(clazz, methodName, parameterClasses);
            ReflectionUtils.makeAccessible(method);
            logger.log(Level.FINE, "Reflect method: {0} on service bean: {1} params: {2} for expression:{3}", new Object[]{method, serviceBean, ObjectUtils.getDisplayString(parameterObject), expression});
            return ReflectionUtils.invokeMethod(method, serviceBean, parameterObject);
        }

    }

}
