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
package org.rill.bpm.api.activiti;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.helper.ClassDelegate;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

/**
 * Specify Activiti listener for user fill-in self's extend attributes.
 * 
 * @author mengran
 * @see ActivitiAccessor#TASK_LIFECYCLE_INTERCEPTOR
 * @see #TASK_SERVICE_INVOKE_EXPRESSION
 */
public final class ExtendAttrsClassDelegateAdapter extends ClassDelegate {

    private final Logger logger = Logger.getLogger(getClass().getName());

    private Map<String, String> extendAttrs = new HashMap<String, String>();
    public ExtendAttrsClassDelegateAdapter(String className, List<FieldDeclaration> fieldDeclarations) {
        super(className, fieldDeclarations);

        for (FieldDeclaration fd : fieldDeclarations) {

            String value = null;
            if (fd.getType().equals(Expression.class.getName())) {
                value = ((Expression) fd.getValue()).getExpressionText();
            } else {
                throw new ActivitiException("Field declarations type[" + fd.getType() +
                        "] not equals" + Expression.class.getName());
            }
            extendAttrs.put(fd.getName().trim(), value);
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

    public final Map<String, String> getExtendAttrs() {

        return extendAttrs;
    }

}
