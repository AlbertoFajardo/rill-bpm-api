/**
 * 
 */
package com.baidu.rigel.service.workflow.api.exception;

import com.baidu.rigel.service.workflow.api.WorkflowOperations;


/**
 * 公司名：百度 <br>
 * 系统名：Rigel直销系统<br>
 * 子系统名: <br>
 * 模块名：HT-SUPPORT <br>
 * 文件名：ProcessDistributeTransactionException.java<br>
 * 功能说明: 工作流和业务系统集成时分布式事务异常<br>
 * @author rillmeng
 * @version 
 * @date 2010-3-26下午07:46:38
**/
public class ProcessDistributeTransactionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8865343609985450022L;

	public static enum EXCEPTION_TRIGGER_ACTION {
		START_PROCESS_INSTANCE, SUSPEND_PROCESS_INSTANCE, RESUME_PROCESS_INSTANCE, TERMINAL_PROCESS_INSTANCE, 
		COMPLETE_TASK_INSTANCE, INIT_TASK_INSTANCE, AFTER_COMPLETE;
	}
	
	public static EXCEPTION_TRIGGER_ACTION convertWorkflowOperation(WorkflowOperations.PROCESS_OPERATION_TYPE type) {
		
		if (type.equals(WorkflowOperations.PROCESS_OPERATION_TYPE.RESUME))
			return EXCEPTION_TRIGGER_ACTION.RESUME_PROCESS_INSTANCE;
		else if (type.equals(WorkflowOperations.PROCESS_OPERATION_TYPE.SUSPEND))
			return EXCEPTION_TRIGGER_ACTION.SUSPEND_PROCESS_INSTANCE;
		else if (type.equals(WorkflowOperations.PROCESS_OPERATION_TYPE.TERMINAL))
			return EXCEPTION_TRIGGER_ACTION.SUSPEND_PROCESS_INSTANCE;
		else {
			throw new IllegalArgumentException("Not support operation[" + type.name() + "]");
		}
	}
	
	private String processInstanceId;
	private String taskInstanceId;
	private EXCEPTION_TRIGGER_ACTION eta;
	private String operator;

	public ProcessDistributeTransactionException(String operator, String processInstanceId,
			String taskInstanceId, EXCEPTION_TRIGGER_ACTION eta, Throwable t) {
		super(t);
		this.processInstanceId = processInstanceId;
		this.taskInstanceId = taskInstanceId;
		this.eta = eta;	
		this.operator = operator;
	}

	/**
	 * @return the processInstanceId
	 */
	public String getProcessInstanceId() {
		return processInstanceId;
	}

	/**
	 * @return the taskInstanceId
	 */
	public String getTaskInstanceId() {
		return taskInstanceId;
	}

	/**
	 * @return the eta
	 */
	public EXCEPTION_TRIGGER_ACTION getEta() {
		return eta;
	}

	/**
	 * @return the operator
	 */
	public final String getOperator() {
		return operator;
	}
	
	

}
