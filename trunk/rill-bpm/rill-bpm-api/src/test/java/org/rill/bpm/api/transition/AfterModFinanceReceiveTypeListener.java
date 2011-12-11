/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rill.bpm.api.transition;

import java.util.logging.Logger;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.rill.bpm.api.activiti.RetrieveNextTasksHelper.TransitionTakeEventListener;
import org.rill.bpm.api.processvar.DummyReceiptInfo;

/**
 *
 * @author mengran
 */
public class AfterModFinanceReceiveTypeListener extends TransitionTakeEventListener {

    static final Logger log = Logger.getLogger(AfterModFinanceReceiveTypeListener.class.getName());
    
    @Override
    public void onTransitionTake(DelegateExecution execution, String processInstanceId, TransitionImpl transition) {
        
        // Delegate this Operation
        restart(execution, processInstanceId);
    }
    
    private void restart(DelegateExecution execution, String processInstanceId) {

        log.fine("Begin call java service task [restart].");
        TaskService taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
        RuntimeService runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
        TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstanceId);

        Task completeFinanceInfo = null;
        for(Task t : taskQuery.list()) {
            if ("完善财务信息".equals(t.getName())) {
                completeFinanceInfo = t;
            }
        }
        if (completeFinanceInfo == null) {
            throw new ActivitiException("Can not found completeFinanceInfo task?");
        }
        // Delete task
//        log.fine("Delete task id:" + completeFinanceInfo.getId());
//        taskService.deleteTask(completeFinanceInfo.getId(), true);
        // Comment by MENGRAN for cause exception during flushing.
        Execution e = runtimeService.createExecutionQuery().executionId(completeFinanceInfo.getExecutionId()).singleResult();
//        runtimeService.setVariable(e.getProcessInstanceId(), "restartFinanceProcess", 1);
//        // Comment by MENGRAN for cause exception during flushing.
//        Map<Class< ? >, Session> sessions = Context.getCommandContext().getSessions();
//        for (Session session : sessions.values()) {
//            session.flush();
//        }
        log.fine("MUST: change variable of execution, runtimeService.setVariable() is not effect.");
        execution.setVariable("restartFinanceProcess", 1);
        if (e instanceof ActivityExecution) {
            log.fine("end the existed finance sub-process");
            ((ActivityExecution) e).end();
        }

        ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (instance == null) {
            throw new ActivitiException("ProcessInstance has ended. So ActivityExecution.end() method can not instead of JUMP_INSTANCE.");
        }

        log.fine("Change process variables for create PRE_INVOICE task");

        execution.setVariable("__receiptInfo_receiptType", 0);

    }
    
}
