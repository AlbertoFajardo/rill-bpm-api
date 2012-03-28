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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.Condition;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.helper.ClassDelegate;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.BpmnParseListener;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.UelExpressionCondition;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.variable.VariableDeclaration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.api.ThreadLocalResourceHolder;
import org.rill.bpm.api.exception.ProcessException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.util.CollectionUtils;


/**
 * Retrieve generated-task and put it on thread.
 * <p>
 * 	Add auto awire transition take listener mechanism.
 * @author mengran
 */
public class RetrieveNextTasksHelper implements BpmnParseListener, InitializingBean, BeanFactoryAware {

	private boolean autoAwireTransactionTakeListener = true;
	
    public final boolean isAutoAwireTransactionTakeListener() {
		return autoAwireTransactionTakeListener;
	}

	public final void setAutoAwireTransactionTakeListener(
			boolean autoAwireTransactionTakeListener) {
		this.autoAwireTransactionTakeListener = autoAwireTransactionTakeListener;
	}

    private List<TransitionTakeEventListener> transitionTakeEventListener = new ArrayList<TransitionTakeEventListener>();

    public List<TransitionTakeEventListener> getTransitionTakeEventListener() {
        return transitionTakeEventListener;
    }

    public void setTransitionTakeEventListener(List<TransitionTakeEventListener> transitionTakeEventListener) {
        this.transitionTakeEventListener = transitionTakeEventListener;
    }
    

	@Override
	public void afterPropertiesSet() throws Exception {
		
		if (!isAutoAwireTransactionTakeListener()) {
			logger.info("Disabled auto awire transaction take listener, so return.");
			return;
		}
		
		Map<String, TransitionTakeEventListener> map = ((ListableBeanFactory) cacheBeanFactory).getBeansOfType(TransitionTakeEventListener.class);
		if (CollectionUtils.isEmpty(map)) return;
		
		if (getTransitionTakeEventListener() == null) {
			setTransitionTakeEventListener(new ArrayList<RetrieveNextTasksHelper.TransitionTakeEventListener>());
		}
		
		for (Entry<String, TransitionTakeEventListener> entry : map.entrySet()) {
			logger.debug("Find " + entry.getValue().getClass() + " named " + entry.getKey() + ", and add to transion take listeners.");
			getTransitionTakeEventListener().add(entry.getValue());
		}
	}
	
	private BeanFactory cacheBeanFactory;
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		
		cacheBeanFactory = beanFactory;
	}

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
    protected static final Log logger = LogFactory.getLog(RetrieveNextTasksHelper.class.getName());
    private static final Integer EXPERIENCE_TASK_COUNT = 3;

    private static class TaskEventListener implements TaskListener {

        private static final TaskEventListener CREATE_LISTENER = new TaskEventListener(TaskListener.EVENTNAME_CREATE);
        private static final TaskEventListener COMPLETE_LISTENER = new TaskEventListener(TaskListener.EVENTNAME_COMPLETE);
        protected final Log logger = LogFactory.getLog(getClass().getName());
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

            logger.debug("Start handle task create event, task id:" + delegateTask.getId());
            // Obtain trigger thread variable
            handleProcessStart(delegateTask);

            // Retrieve lastest element
            Stack<Map<String, Set<String>>> trigger = (Stack<Map<String, Set<String>>>) ThreadLocalResourceHolder.getProperty(TASK_GATHER);
            Map<String, Set<String>> peek = trigger.peek();
            String key = peek.keySet().iterator().next();
            logger.debug("Retrive thread-binding trgger key:" + key);
            Set<String> creationTaskIds = peek.get(key);
            creationTaskIds.add(delegateTask.getId());
            logger.debug("Add current creation task id:" + delegateTask.getId() + " to " + creationTaskIds + ", key:" + key);
        }

        void handleTaskComplete(DelegateTask delegateTask) {

            logger.debug("Start handle task complete event, task id:" + delegateTask.getId());
            // Obtain trigger thread variable
            Stack<Map<String, Set<String>>> trigger = (Stack<Map<String, Set<String>>>) ThreadLocalResourceHolder.getProperty(TASK_GATHER);
            Map<String, Set<String>> element = new HashMap<String, Set<String>>();
            String key = delegateTask.getId();
            Set<String> creationTaskIds = new LinkedHashSet<String>(EXPERIENCE_TASK_COUNT);
            element.put(key, creationTaskIds);
            trigger.push(element);
            logger.debug("Push current task scope:" + delegateTask.getId() + ", key:" + key);
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
            logger.debug("No thread binding task scope yet, we new/bind it.");
            // Means process start case, and set
            taskScope = new Stack<Map<String, Set<String>>>();
            ThreadLocalResourceHolder.bindProperty(TASK_GATHER, taskScope);
        }
        // Add to stack top position
        Map<String, Set<String>> element = new HashMap<String, Set<String>>();
        element.put(trigger, new LinkedHashSet<String>(EXPERIENCE_TASK_COUNT));
        logger.debug("Push task scope trigger:" + trigger);
        taskScope.push(element);
    }

    public static List<String> popTaskScope(String trigger) {

        Stack<Map<String, Set<String>>> taskScope = (Stack<Map<String, Set<String>>>) ThreadLocalResourceHolder.getProperty(TASK_GATHER);
        if (taskScope == null) {
            logger.error("Pop task scope return null because there is nothing in thread-binding variable");
            throw new ProcessException("Pop task scope return null because there is nothing in thread-binding variable");
        }

        // Gather first element of stack temporarily
        Set<String> taskGather = new LinkedHashSet<String>(EXPERIENCE_TASK_COUNT * EXPERIENCE_TASK_COUNT);
        // Peek task in scope
        boolean meetTrigger = false;
        while (!meetTrigger) {
            Map<String, Set<String>> element = taskScope.pop();
            String key = element.keySet().iterator().next();
            logger.debug("Find task scope key:" + key + ", gather tasks:" + element.get(key));
            taskGather.addAll(element.get(key));
            meetTrigger = trigger.equals(key);
        }

        logger.debug("Pop task scope key:" + trigger + ", gather tasks:" + taskGather);
        return new ArrayList<String>(taskGather);
    }

    @SuppressWarnings("unchecked")
	public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {

        UserTaskActivityBehavior uab = (UserTaskActivityBehavior) activity.getActivityBehavior();
        TaskDefinition td = uab.getTaskDefinition();

        // Adapt Activiti listener mechanism as use's self extend attributes holder
        List<TaskListener> classDelegateAdapters = new ArrayList<TaskListener>();
        Map<String, List<TaskListener>> activitiInternalTaskListenerMap = new HashMap<String, List<TaskListener>>();
        if (td.getTaskListeners() != null && td.getTaskListeners().size() > 0) {
            for (Entry<String, List<TaskListener>> entry : td.getTaskListeners().entrySet()) {
            	List<TaskListener> value = entry.getValue(); 
            	List<TaskListener> activitiInternalTaskListener = new ArrayList<TaskListener>();
                if (value != null && !value.isEmpty()) {
                    for (TaskListener tl : value) {
                        if (ClassDelegate.class.isInstance(tl)) {
                            try {
                                Field classNameField = ReflectUtil.getField("className", tl);
                                classNameField.setAccessible(true);
                                if (classNameField == null ||
                                        !ExtendAttrsClassDelegateAdapter.class.getName().equals(classNameField.get(tl).toString())) {
                                    throw new ActivitiException("Can not reflect protected field [className]/value not equals " +
                                            ExtendAttrsClassDelegateAdapter.class.getName() + " on " + tl);
                                }
                                Field fieldDeclarations = ReflectUtil.getField("fieldDeclarations", tl);
                                fieldDeclarations.setAccessible(true);
                                ExtendAttrsClassDelegateAdapter adapter = new ExtendAttrsClassDelegateAdapter(ExtendAttrsClassDelegateAdapter.class.getName(),
                                        (List<FieldDeclaration>) fieldDeclarations.get(tl));
                                classDelegateAdapters.add(adapter);
                            } catch (IllegalArgumentException ex) {
                                logger.error("Exception occurred when try to retrieve extend attributes", ex);
                            } catch (IllegalAccessException ex) {
                            	logger.error("Exception occurred when try to retrieve extend attributes", ex);
                            }
                            
                        } else if (tl.getClass().getName().startsWith("org.activiti")) {
                            logger.debug("Ignore activiti engine's task listener.");
                            activitiInternalTaskListener.add(tl);
                        } else {
                            throw new ActivitiException("We not allow use Activiti's task listener mechanism.");
                        }
                    }
                }
                // Put it for re-add
                activitiInternalTaskListenerMap.put(entry.getKey(), activitiInternalTaskListener);
            }
        }
        // Clear modeled task listeners and add our listeners
        td.getTaskListeners().clear();
        for (TaskListener listener : classDelegateAdapters) {
            td.addTaskListener(TaskListener.EVENTNAME_CREATE, listener);
        }
        td.addTaskListener(TaskListener.EVENTNAME_CREATE, TaskEventListener.CREATE_LISTENER);
        td.addTaskListener(TaskListener.EVENTNAME_COMPLETE, TaskEventListener.COMPLETE_LISTENER);
        
        // Add activiti's internal task listener
        for (Entry<String, List<TaskListener>> entry : activitiInternalTaskListenerMap.entrySet()) {
        	for (TaskListener element : entry.getValue()) {
        		td.addTaskListener(entry.getKey(), element);
        	}
        }

    }

    public static final String GO_BACK_FEATURE = "__back_able";
    public static final String GO_BACK_VARIABLE = "__go_back";
    public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {

        // Add transition [take] event listener
        if (!CollectionUtils.isEmpty(getTransitionTakeEventListener())) {
            for (TransitionTakeEventListener ttel : getTransitionTakeEventListener()) {
                logger.debug("Add transition take event listener:" +  ttel);
                transition.addExecutionListener(ttel);
            }
        }
        
        // Add go-back feature [__back_able]
        String __go_back = GO_BACK_VARIABLE;
        String id = sequenceFlowElement.attribute("id");
        String sourceRef = sequenceFlowElement.attribute("sourceRef");
        String destinationRef = sequenceFlowElement.attribute("targetRef");
        ActivityImpl sourceActivity = scopeElement.findActivity(sourceRef);
        ActivityImpl destinationActivity = scopeElement.findActivity(destinationRef);
        
        // Set default property if have only one out-going transitions
        List<PvmTransition> outTransitions = sourceActivity.getOutgoingTransitions();
        if (outTransitions.size() == 2) {
        	int goBackTransitionIndex  = -1;
	        for (int i = 0; i < outTransitions.size(); i++) {
	        	if (outTransitions.get(i).getId().endsWith(__go_back)) {
	        		goBackTransitionIndex = i;
	        	}
	        }
	        if (goBackTransitionIndex >= 0) {
	        	sourceActivity.setProperty("default", outTransitions.get(1 - goBackTransitionIndex).getId());
	        }
        }
        if (id.startsWith(GO_BACK_FEATURE)) {
	        TransitionImpl gobackTransition = destinationActivity.createOutgoingTransition(id + __go_back);
	        gobackTransition.setProperty("name", sequenceFlowElement.attribute("name") + __go_back);
	        gobackTransition.setProperty("documentation", __go_back);
	        gobackTransition.setDestination(sourceActivity);
	        String expression = "${" + __go_back + "==true}";
	        
	        // Bugfix for scale-out feature. Add test-case ScaleoutTest to prevent same bug. Add by MENGRAN at 2012-03-28
	        ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
	        Condition expressionCondition = new UelExpressionCondition(processEngineConfiguration.getExpressionManager().createExpression(expression));
	        gobackTransition.setProperty(BpmnParse.PROPERTYNAME_CONDITION_TEXT, expression);
	        gobackTransition.setProperty(BpmnParse.PROPERTYNAME_CONDITION, expressionCondition);
	        
	        // Initialize go-back flag
	        transition.addExecutionListener(gobackTransitionTakeListener);
	        // Re-set go-back flag
	        gobackTransition.addExecutionListener(gobackTransitionTakeListener);
        }
        
    }
    
    private TransitionTakeEventListener gobackTransitionTakeListener = new TransitionTakeEventListener() {

		@Override
		public void onTransitionTake(DelegateExecution execution,
				String processInstanceId, TransitionImpl transition) {
			
			logger.debug("Set process instance[" + processInstanceId + "] GO_BACK_VARIABLE to false after it taken " + transition);
			execution.setVariable(GO_BACK_VARIABLE, "false");
		}
    	
    };

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
