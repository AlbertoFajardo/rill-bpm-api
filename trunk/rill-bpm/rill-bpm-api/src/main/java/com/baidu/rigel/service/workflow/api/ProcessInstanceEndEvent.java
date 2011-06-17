package com.baidu.rigel.service.workflow.api;

import org.springframework.context.ApplicationEvent;

/**
 * 公司名：百度 <br>
 * 系统名：Rigel直销系统<br>
 * 子系统名: <br>
 * 模块名：HT-SUPPORT <br>
 * 文件名：ProcessInstanceEndEvent.java<br>
 * 功能说明: 流程结束事件<br>
 * @author mengran
 * @version 1.0.0
 * @date 2010-6-4下午06:08:33
**/
public class ProcessInstanceEndEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4064636834454294813L;

	private String triggerTaskInstanceId;
	private boolean hasParentProcess = false;
	private Object triggerTaskExecutionContext;
	
	/**
	 * @return the triggerTaskExecutionContext
	 */
	public final Object getTriggerTaskExecutionContext() {
		return triggerTaskExecutionContext;
	}

	public boolean isHasParentProcess() {
		return hasParentProcess;
	}

	/**
	 * @return the triggerTaskInstanceId
	 */
	public String getTriggerTaskInstanceId() {
		return triggerTaskInstanceId;
	}

	/**
	 * @return the processInstanceId
	 */
	public String getProcessInstanceId() {
		return (String) source;
	}

	public ProcessInstanceEndEvent(Object source) {
		super(source);
	}
	
	public ProcessInstanceEndEvent(Object source, String triggerTaskInstanceId) {
		super(source);
		
		this.triggerTaskInstanceId = triggerTaskInstanceId;
	}
	
	public ProcessInstanceEndEvent(Object source, String triggerTaskInstanceId, boolean hasParentProcess) {
		this(source, triggerTaskInstanceId);
		
		this.hasParentProcess = hasParentProcess;
	}
	
	public ProcessInstanceEndEvent(Object source, String triggerTaskInstanceId, 
			boolean hasParentProcess, Object taskExecutionContext) {
		this(source, triggerTaskInstanceId, hasParentProcess);
		
		this.triggerTaskExecutionContext = taskExecutionContext;
	} 
}
