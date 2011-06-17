/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.baidu.rigel.service.workflow.api.servicetask;

import java.io.Serializable;
import java.util.logging.Logger;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import com.baidu.rigel.service.workflow.api.processvar.ReceiptInfo;

/**
 *
 * @author mengran
 */
public class RestartFinanceProcessServiceTask implements Serializable {

    static final Logger log = Logger.getLogger(RestartFinanceProcessServiceTask.class.getName());
    
    public void restart(DelegateExecution execution) {

        log.fine("Begin call java service task [restart].");
        TaskService taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
        RuntimeService runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
        TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(execution.getProcessInstanceId());

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

        String processInstanceId = execution.getProcessInstanceId();
        ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (instance == null) {
            throw new ActivitiException("ProcessInstance has ended. So ActivityExecution.end() method can not instead of JUMP_INSTANCE.");
        }

        log.fine("Change process variables for create PRE_INVOICE task");


        ReceiptInfo receiptInfo = (ReceiptInfo) execution.getVariable("receiptInfo");
        receiptInfo.setReceiptType(ReceiptInfo.PRE_INVOICE);
        execution.setVariable("receiptInfo", receiptInfo);

    }

    public void recycle(DelegateExecution execution) {

        log.fine("In service-task change variable of execution is effect way.");
        execution.setVariable("restartFinanceProcess", -1);
        log.fine("call java service task [recycle] successfully.");
//        RuntimeService runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
//        runtimeService.setVariable(execution.getProcessInstanceId(), "restartFinanceProcess", -1);
    }
}
