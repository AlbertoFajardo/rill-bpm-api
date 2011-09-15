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

import com.baidu.rigel.service.workflow.api.ThreadLocalResourceHolder;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.helper.ClassDelegate;
import org.activiti.engine.impl.bpmn.parser.BpmnParseListener;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.variable.VariableDeclaration;
import org.springframework.util.CollectionUtils;

/**
 * Retrieve generated-task and put it on thread.
 * @author mengran
 */
public class RetrieveNextTasksHelper implements BpmnParseListener {

    public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
        // Do nothing
    }

    public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity) {
        // Do nothing
    }

    public void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
        // Do nothing
    }

    public void parseParallelGateway(Element parallelGwElement, ScopeImpl scope, ActivityImpl activity) {
        // Do nothing
    }

    public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
        // Do nothing
    }

    public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
        // Do nothing
    }

    public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
        // Do nothing
    }

    public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
        // Do nothing
    }

    public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
        // Do nothing
    }

    public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
        // Do nothing
    }

    public void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting, ActivityImpl timerActivity) {
        // Do nothing
    }

    public void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting, ActivityImpl activity, ActivityImpl nestedErrorEventActivity) {
        // Do nothing
    }

    public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
        // Do nothing
    }

    public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
        // Do nothing
    }

    public void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, ActivityImpl activity) {
        // Do nothing
    }

    public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
        // Do nothing
    }

    public void parseMultiInstanceLoopCharacteristics(Element activityElement, Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {
        // Do nothing
    }

    public void parseIntermediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity) {
        // Do nothing
    }
    
    // ----------------------------------- My implementation --------------------------------------- //
    private static final String TASK_GATHER = TaskEventListener.class.getName() + ".TASK_TRIGGER";
    private static final Logger logger = Logger.getLogger(RetrieveNextTasksHelper.class.getName());
    private static final Integer EXPERIENCE_TASK_COUNT = 3;

    private static class TaskEventListener implements TaskListener {

        private static final TaskEventListener CREATE_LISTENER = new TaskEventListener(TaskListener.EVENTNAME_CREATE);
        private static final TaskEventListener COMPLETE_LISTENER = new TaskEventListener(TaskListener.EVENTNAME_COMPLETE);
        private static final Logger logger = Logger.getLogger(TaskEventListener.class.getName());
        private String eventName;

        private TaskEventListener(String eventName) {

            this.eventName = eventName;
        }

        public void notify(DelegateTask delegateTask) {

            if (TaskListener.EVENTNAME_CREATE.equals(this.eventName)) {
                handleTaskCreate(delegateTask);
            } else if (TaskEventListener.EVENTNAME_COMPLETE.equals(this.eventName)) {
                handleTaskComplete(delegateTask);
            } else {
                throw new IllegalArgumentException(eventName);
            }
        }

        void handleTaskCreate(DelegateTask delegateTask) {

            logger.log(Level.FINEST, "Start handle task create event, task id:{0}", delegateTask.getId());
            // Obtain trigger thread variable
            handleProcessStart(delegateTask);

            // Retrieve lastest element
            Stack<Map<String, Set<String>>> trigger = (Stack<Map<String, Set<String>>>) ThreadLocalResourceHolder.getProperty(TASK_GATHER);
            Map<String, Set<String>> peek = trigger.peek();
            String key = peek.keySet().iterator().next();
            logger.log(Level.FINEST, "Retrive thread-binding trgger key:{0}", key);
            Set<String> creationTaskIds = peek.get(key);
            creationTaskIds.add(delegateTask.getId());
            logger.log(Level.FINE, "Add current creation task id:{0} to {1}, key:{2}", new Object[]{delegateTask.getId(), creationTaskIds, key});
        }

        void handleTaskComplete(DelegateTask delegateTask) {

            logger.log(Level.FINEST, "Start handle task complete event, task id:{0}", delegateTask.getId());
            // Obtain trigger thread variable
            Stack<Map<String, Set<String>>> trigger = (Stack<Map<String, Set<String>>>) ThreadLocalResourceHolder.getProperty(TASK_GATHER);
            Map<String, Set<String>> element = new HashMap<String, Set<String>>();
            String key = delegateTask.getId();
            Set<String> creationTaskIds = new LinkedHashSet<String>(EXPERIENCE_TASK_COUNT);
            element.put(key, creationTaskIds);
            trigger.push(element);
            logger.log(Level.FINE, "Push current task scope:{0}, key:{1}", new Object[]{delegateTask.getId(), key});
        }

        void handleProcessStart(DelegateTask delegateTask) {

            Stack<Map<String, Set<String>>> trigger = (Stack<Map<String, Set<String>>>) ThreadLocalResourceHolder.getProperty(TASK_GATHER);
            if (trigger == null) {
                // Push null as process start trigger.
                pushTaskScope("null");
            }
        }
    }

    public static void pushTaskScope(String trigger) {

        Stack<Map<String, Set<String>>> taskScope = (Stack<Map<String, Set<String>>>) ThreadLocalResourceHolder.getProperty(TASK_GATHER);
        if (taskScope == null) {
            logger.finest("No thread binding task scope yet, we new/bind it.");
            // Means process start case, and set
            taskScope = new Stack<Map<String, Set<String>>>();
            ThreadLocalResourceHolder.bindProperty(TASK_GATHER, taskScope);
        }
        // Add to stack top position
        Map<String, Set<String>> element = new HashMap<String, Set<String>>();
        element.put(trigger, new LinkedHashSet<String>(EXPERIENCE_TASK_COUNT));
        logger.log(Level.FINEST, "Push task scope trigger:{0}", trigger);
        taskScope.push(element);
    }

    public static List<String> popTaskScope(String trigger) {

        Stack<Map<String, Set<String>>> taskScope = (Stack<Map<String, Set<String>>>) ThreadLocalResourceHolder.getProperty(TASK_GATHER);
        if (taskScope == null) {
            logger.severe("Pop task scope return null because there is nothing in thread-binding variable");
            throw new ProcessException("Pop task scope return null because there is nothing in thread-binding variable");
        }

        // Gather first element of stack temporarily
        Set<String> taskGather = new LinkedHashSet<String>(EXPERIENCE_TASK_COUNT * EXPERIENCE_TASK_COUNT);
        // Peek task in scope
        boolean meetTrigger = false;
        while (!meetTrigger) {
            Map<String, Set<String>> element = taskScope.pop();
            String key = element.keySet().iterator().next();
            logger.log(Level.FINEST, "Find task scope key:{0}, gather tasks:{1}", new Object[]{key, element.get(key)});
            taskGather.addAll(element.get(key));
            meetTrigger = trigger.equals(key);
        }

        logger.log(Level.FINE, "Pop task scope key:{0}, gather tasks:{1}", new Object[]{trigger, taskGather});
        return new ArrayList<String>(taskGather);
    }

    public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {

        UserTaskActivityBehavior uab = (UserTaskActivityBehavior) activity.getActivityBehavior();
        TaskDefinition td = uab.getTaskDefinition();

        // Adapt Activiti listener mechanism and TLI/TOI mechanism
        List<TaskListener> classDelegateAdapters = new ArrayList<TaskListener>();
        if (td.getTaskListeners() != null && td.getTaskListeners().size() > 0) {
            for (List<TaskListener> value : td.getTaskListeners().values()) {
                if (value != null && !value.isEmpty()) {
                    for (TaskListener tl : value) {
                        if (ClassDelegate.class.isInstance(tl)) {
                            try {
                                Field classNameField = ReflectUtil.getField("className", tl);
                                classNameField.setAccessible(true);
                                if (classNameField == null ||
                                        !ClassDelegateAdapter.class.getName().equals(classNameField.get(tl).toString())) {
                                    throw new ActivitiException("Can not reflect protected field [className]/value not equals " +
                                            ClassDelegateAdapter.class.getName() + " on " + tl);
                                }
                                Field fieldDeclarations = ReflectUtil.getField("fieldDeclarations", tl);
                                fieldDeclarations.setAccessible(true);
                                ClassDelegateAdapter adapter = new ClassDelegateAdapter(ClassDelegateAdapter.class.getName(),
                                        (List<FieldDeclaration>) fieldDeclarations.get(tl));
                                classDelegateAdapters.add(adapter);
                            } catch (IllegalArgumentException ex) {
                                logger.log(Level.SEVERE, null, ex);
                            } catch (IllegalAccessException ex) {
                                logger.log(Level.SEVERE, null, ex);
                            }
                        } else if (tl.getClass().getName().startsWith("org.activiti")) {
                            logger.log(Level.FINE ,"Ignore activiti engine's task listener.");
                        } else {
                            throw new ActivitiException("We not allow use Activiti's task listener mechanism.");
                        }
                    }
                }
            }
        }
        // Clear modeled task listeners and add our listeners
        td.getTaskListeners().clear();
        for (TaskListener listener : classDelegateAdapters) {
            td.addTaskListener(TaskListener.EVENTNAME_CREATE, listener);
        }
        td.addTaskListener(TaskListener.EVENTNAME_CREATE, TaskEventListener.CREATE_LISTENER);
        td.addTaskListener(TaskListener.EVENTNAME_COMPLETE, TaskEventListener.COMPLETE_LISTENER);


    }

    public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {

        // Add transition [take] event listener
        if (!CollectionUtils.isEmpty(getTransitionTakeEventListener())) {
            for (TransitionTakeEventListener ttel : getTransitionTakeEventListener()) {
                logger.log(Level.FINEST, "Add transition take event listener:{0}.", ttel);
                transition.addExecutionListener(ttel);
            }
        }

    }

    private List<TransitionTakeEventListener> transitionTakeEventListener = new ArrayList<TransitionTakeEventListener>();

    public List<TransitionTakeEventListener> getTransitionTakeEventListener() {
        return transitionTakeEventListener;
    }

    public void setTransitionTakeEventListener(List<TransitionTakeEventListener> transitionTakeEventListener) {
        this.transitionTakeEventListener = transitionTakeEventListener;
    }


    public static abstract class TransitionTakeEventListener implements ExecutionListener {

        public final void notify(DelegateExecution execution) throws Exception {

            if (InterpretableExecution.class.isAssignableFrom(execution.getClass())) {

                InterpretableExecution interpretableExecution = (InterpretableExecution) execution;
                onTransitionTake(execution, interpretableExecution.getProcessInstanceId() , interpretableExecution.getTransition());
            }
        }

        public abstract void onTransitionTake(DelegateExecution execution, String processInstanceId, TransitionImpl transition);

    }
}
