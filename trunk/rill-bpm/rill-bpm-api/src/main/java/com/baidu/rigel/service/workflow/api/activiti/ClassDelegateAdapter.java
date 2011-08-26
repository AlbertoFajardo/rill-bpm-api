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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.helper.ClassDelegate;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.springframework.util.Assert;

/**
 * Specify Activiti listener for adapt t-com's workflow task-lifecycle-interceptor(TLI)/task-operation-interceptor(TOI) and other informations.
 * 
 * @author mengran
 * @see ActivitiAccessor#TASK_LIFECYCLE_INTERCEPTOR
 * @see ActivitiAccessor#TASK_OPERATION_INTERCEPTOR
 * @see #TASK_SERVICE_INVOKE_EXPRESSION
 */
public final class ClassDelegateAdapter extends ClassDelegate {

    private static final Logger logger = Logger.getLogger(ClassDelegateAdapter.class.getName());

    private static final String TASK_SERVICE_INVOKE_EXPRESSION = ActivitiAccessor.TASK_SERVICE_INVOKE_EXPRESSION;
    
    private Collection<String> taskLifycycleInterceptors = new LinkedHashSet(1);
    private Collection<String> taskOperationInterceptors = new LinkedHashSet(1);
    private Collection<String> taskServiceInvokeExpression = new LinkedHashSet(1);

    public ClassDelegateAdapter(String className, List<FieldDeclaration> fieldDeclarations) {
        super(className, fieldDeclarations);

//        Assert.notEmpty(fieldDeclarations, "Please inject fields for task-lifecycle-interceptor/task-operation-interceptor.");
        for (FieldDeclaration fd : fieldDeclarations) {

            String value = null;
            if (fd.getType().equals(Expression.class.getName())) {
                value = ((Expression) fd.getValue()).getExpressionText();
            } else {
                throw new ActivitiException("Field declarations type[" + fd.getType() +
                        "] not equals" + Expression.class.getName());
            }
            if (ActivitiAccessor.TASK_LIFECYCLE_INTERCEPTOR.equals(fd.getName().trim())) {
                taskLifycycleInterceptors.add(value);
            }
            if (ActivitiAccessor.TASK_OPERATION_INTERCEPTOR.equals(fd.getName().trim())) {
                taskOperationInterceptors.add(value);
            }
            if (TASK_SERVICE_INVOKE_EXPRESSION.equals(fd.getName().trim())) {
                taskServiceInvokeExpression.add(value);
            }
        }

        Assert.notNull(taskLifycycleInterceptors, "Please inject fields for task-lifecycle-interceptor.");
        if (taskOperationInterceptors == null) {
            logger.fine("Not found injected field for task-operation-interceptor.");
        }
        if (taskServiceInvokeExpression == null) {
            logger.fine("Not found injected field for taskServiceInvokeExpression.");
        }
    }

    @Override
    public void notify(DelegateExecution execution) throws Exception {

        logger.fine("Do nothing when TLIClassDelegateAdapter as a execution listener.");
    }

    @Override
    public void notify(DelegateTask delegateTask) {

        logger.fine("Do nothing when TLIClassDelegateAdapter as a task listener.");
    }

    @Override
    public void execute(ActivityExecution execution) throws Exception {

        logger.fine("Do nothing when TLIClassDelegateAdapter as a activity behavior.");
    }

    public final Collection<String> obtainTaskLifycycleInterceptors() {

        return taskLifycycleInterceptors;
    }

    public final Collection<String> obtainTaskOperationInterceptors() {

        return taskOperationInterceptors;
    }

    public Collection<String> getTaskServiceInvokeExpression() {
        return taskServiceInvokeExpression;
    }

}
