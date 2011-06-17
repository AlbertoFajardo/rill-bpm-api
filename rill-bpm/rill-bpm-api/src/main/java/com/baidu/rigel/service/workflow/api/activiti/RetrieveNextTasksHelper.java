/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.BpmnParseListener;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.variable.VariableDeclaration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
    private boolean serializeVarPermission = true;

    public void setSerializeVarPermission(boolean serializeVarPermission) {
        this.serializeVarPermission = serializeVarPermission;
    }

    public boolean isSerializeVarPermission() {
        return serializeVarPermission;
    }

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

            logger.finest("Start handle task create event, task id:" + delegateTask.getId());
            // Obtain trigger thread variable
            handleProcessStart(delegateTask);

            // Retrieve lastest element
            Stack<Map<String, Set<String>>> trigger = (Stack<Map<String, Set<String>>>) ThreadLocalResourceHolder.getProperty(TASK_GATHER);
            Map<String, Set<String>> peek = trigger.peek();
            String key = peek.keySet().iterator().next();
            logger.finest("Retrive thread-binding trgger key:" + key);
            Set<String> creationTaskIds = peek.get(key);
            creationTaskIds.add(delegateTask.getId());
            logger.fine("Add current creation task id:" + delegateTask.getId() + " to " + creationTaskIds + ", key:" + key);
        }

        void handleTaskComplete(DelegateTask delegateTask) {

            logger.finest("Start handle task complete event, task id:" + delegateTask.getId());
            // Obtain trigger thread variable
            Stack<Map<String, Set<String>>> trigger = (Stack<Map<String, Set<String>>>) ThreadLocalResourceHolder.getProperty(TASK_GATHER);
            Map<String, Set<String>> element = new HashMap<String, Set<String>>();
            String key = delegateTask.getId();
            Set<String> creationTaskIds = new LinkedHashSet<String>(EXPERIENCE_TASK_COUNT);
            element.put(key, creationTaskIds);
            trigger.push(element);
            logger.fine("Push current task scope:" + delegateTask.getId() + ", key:" + key);
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
        logger.finest("Push task scope trigger:" + trigger);
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
            logger.finest("Find task scope key:" + key + ", gather tasks:" + element.get(key));
            taskGather.addAll(element.get(key));
            meetTrigger = trigger.equals(key);
        }

        logger.fine("Pop task scope key:" + trigger + ", gather tasks:" + taskGather);
        return new ArrayList<String>(taskGather);
    }

    public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {

        UserTaskActivityBehavior uab = (UserTaskActivityBehavior) activity.getActivityBehavior();
        TaskDefinition td = uab.getTaskDefinition();

        // Adapt Activiti listener mechanism and TLI/TOI mechanism
        List<TaskListener> tlitoiClassDelegateAdapters = new ArrayList<TaskListener>();
        if (td.getTaskListeners() != null && td.getTaskListeners().size() > 0) {
            for (List<TaskListener> value : td.getTaskListeners().values()) {
                if (value != null && !value.isEmpty()) {
                    for (TaskListener tl : value) {
                        if (ClassDelegate.class.isInstance(tl)) {
                            try {
                                Field classNameField = ReflectUtil.getField("className", tl);
                                classNameField.setAccessible(true);
                                if (classNameField == null &&
                                        !TLITOIClassDelegateAdapter.class.getName().equals(classNameField.get(tl).toString())) {
                                    new ActivitiException("Can not reflect protected field [className]/value not equals " +
                                            TLITOIClassDelegateAdapter.class.getName() + " on " + tl);
                                }
                                Field fieldDeclarations = ReflectUtil.getField("fieldDeclarations", tl);
                                fieldDeclarations.setAccessible(true);
                                TLITOIClassDelegateAdapter adapter = new TLITOIClassDelegateAdapter(TLITOIClassDelegateAdapter.class.getName(),
                                        (List<FieldDeclaration>) fieldDeclarations.get(tl));
                                tlitoiClassDelegateAdapters.add(adapter);
                            } catch (IllegalArgumentException ex) {
                                logger.log(Level.SEVERE, null, ex);
                            } catch (IllegalAccessException ex) {
                                logger.log(Level.SEVERE, null, ex);
                            }
                        } else {
                            new ActivitiException("We not allow use Activiti's task listener mechanism.");
                        }
                    }
                }
            }
        }
        // Clear modeled task listeners and add our listeners
        td.getTaskListeners().clear();
        for (TaskListener listener : tlitoiClassDelegateAdapters) {
            td.addTaskListener(TaskListener.EVENTNAME_CREATE, listener);
        }
        td.addTaskListener(TaskListener.EVENTNAME_CREATE, TaskEventListener.CREATE_LISTENER);
        td.addTaskListener(TaskListener.EVENTNAME_COMPLETE, TaskEventListener.COMPLETE_LISTENER);


    }

    public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {

        // Disallow serialize process variable(not default)
        if (!serializeVarPermission) {
            logger.fine("Detect serialize variagble.");
            String expression = (String) transition.getProperty(BpmnParse.PROPERTYNAME_CONDITION_TEXT);
            if (StringUtils.hasLength(expression) && StringUtils.split(expression, ".") != null) {
                new ActivitiException("Disallow serialize variable[" + expression + "].");
            }
        } else {
            logger.log(Level.WARNING ,"Allow serialize variable, we strongly suggest that control it's size for performance.");
        }

        // Add transition [take] event listener
        if (!CollectionUtils.isEmpty(getTransitionTakeEventListener())) {
            for (TransitionTakeEventListener ttel : getTransitionTakeEventListener()) {
                logger.finest("Add transition take event listener:" + ttel + ".");
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
                onTransitionTake(interpretableExecution.getProcessInstanceId() , interpretableExecution.getTransition());
            }
        }

        public abstract void onTransitionTake(String processInstanceId, TransitionImpl transition);

    }
}
