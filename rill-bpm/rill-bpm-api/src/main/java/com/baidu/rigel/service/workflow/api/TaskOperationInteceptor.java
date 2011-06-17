package com.baidu.rigel.service.workflow.api;

import com.baidu.rigel.service.workflow.api.exception.ProcessException;


/**
 * 公司名：百度 <br>
 * 系统名：Rigel直销系统<br>
 * 子系统名: <br>
 * 模块名：HT-SUPPORT <br>
 * 文件名：ProcessOperationInteceptor.java<br>
 * 功能说明: 任务操作拦截器，允许其他类参与到流程操作的过程中，并有机会阻止当前操作。<br>
 * @author mengran
 * @version 1.0.0
 * @date 2010-5-14下午04:38:39
**/
public interface TaskOperationInteceptor {
	
	/**
	 * @return 操作类型。
	 */
	WorkflowOperations.PROCESS_OPERATION_TYPE handleOpeationType();
	
	void preOperation(Object taskExecutionContext) throws ProcessException;
	
	void postOperation(String engineProcessInstanceId) throws ProcessException;
	
}
