package com.baidu.rigel.service.workflow.api;

import java.util.LinkedHashMap;
import java.util.Map;

import com.baidu.rigel.service.workflow.api.exception.ProcessException;
import java.util.Set;

/**
 * 公司名：百度 <br>
 * 系统名：Rigel直销系统<br>
 * 子系统名: <br>
 * 模块名：HT-SUPPORT <br>
 * 文件名：WorkflowOperations.java<br>
 * 功能说明: 封装了对于工作流的操作，抽象了底层工作流引擎。<br>
 * @author mengran
 * @version 1.0.0
 * @date 2010-5-14上午11:12:55
**/
public interface WorkflowOperations {

	enum PROCESS_OPERATION_TYPE {
		SUSPEND, // 挂起
		RESUME, // 恢复
		TERMINAL // 终止
	}
	/**
         * Task form data key. 
         */
        String ENGINE_DRIVEN_TASK_FORM_DATA_KEY = WorkflowOperations.class.getName() + ".ENGINE_DRIVEN_TASK_FORM_DATA_KEY";
        /**
         * Task return data key.
         */
        String ENGINE_DRIVEN_TASK_RETURN_DATA_KEY = WorkflowOperations.class.getName() + ".ENGINE_DRIVEN_TASK_RETURN_DATA_KEY";

	/**
	 * 启动一个流程实例。
	 * @param modelInfo 流程相关信息，包括流程包ID，流程定义ID，流程名字等信息。
	 * @param processStarterInfo 流程启动者相关信息，包括启动者ID，启动者岗位ID等。
	 * @param businessObjectId 流程对应的业务实体的ID，<code>NOT NULL</code>
	 * @param startParams 流程启动参数。如果启动流程后需要一些参数信息来决定创建任务，则需要传值。
	 * @return 返回工作流引擎创建的流程实例对象
	 * @throws ProcessException 创建流程实例失败时的异常
	 */
	Object createProcessInstance(Object modelInfo, Object processStarterInfo, Long businessObjectId, Map<String, Object> startParams) throws ProcessException;
	
	/**
	 * 终止一个流程实例。
	 * @param engineProcessInstanceId 流程实例ID
	 * @param operator 终止操作人
	 * @param reason 终止原因
	 */
	void terminalProcessInstance(String engineProcessInstanceId, String operator, String reason) throws ProcessException;
	
	/**
	 * 挂起一个流程实例。
	 * @param engineProcessInstanceId 流程实例ID
	 * @param operator 挂起操作人
	 * @param reason 挂起原因
	 */
	void suspendProcessInstance(String engineProcessInstanceId, String operator, String reason) throws ProcessException;
	
	/**
	 * 恢复一个流程实例。
	 * @param engineProcessInstanceId 流程实例ID
	 * @param operator 恢复操作人
	 * @param reason 恢复原因
	 */
	void resumeProcessInstance(String engineProcessInstanceId, String operator, String reason) throws ProcessException;
	
	
	// ------------------------------------ Task related API ------------------------------------ //
	
	/**
	 * 完成任务实例。
	 * @param engineTaskInstanceId 任务实例ID
	 * @param operator 操作人
	 * @param workflowParams 工作流参数。如果完成当前任务后，后续路径需要表达式参数，则需要传值。
	 */
	void completeTaskInstance(String engineTaskInstanceId, String operator, Map<String, Object> workflowParams) throws ProcessException;
	
//	/**
//	 * 嵌套完成任务实例。这个方法是一个临时解决方案。
//	 * @param taskInstanceId 任务实例ID
//	 * @param operator 操作人
//	 * @param workflowParams 工作流参数。如果完成当前任务后，后续路径需要表达式参数，则需要传值。
//	 * @deprecated 临时方案，主要为了适应在一个完成任务中再嵌套的执行一个任务的完成场景。后续会升级线程变量绑定机制并废除这个方法。
//	 */
//	void nestedCompleteTaskInstance(String taskInstanceId, String operator, Map<String, Object> workflowParams) throws ProcessException;
	
//	/**
//	 * 跳转任务实例。
//	 * @param taskInstanceId 任务实例ID
//	 * @param operator 操作人
//	 * @param jumpToTaskDefineId 跳转任务定义ID
//	 */
//	void jumpTaskInstance(String taskInstanceId, String operator, String jumpToTaskDefineId) throws ProcessException;
	
	/**
	 * 批量完成任务实例。
	 * @param batchDTO 批量完成任务DTO对象，是任务实例ID和工作流参数的Map
	 * @param opeartor 操作人
	 */
	void batchCompleteTaskIntances(LinkedHashMap<String, Map<String, Object>> batchDTO, String opeartor) throws ProcessException;
	
	/**
	 * 获得任务实例的扩展属性
	 * @param engineTaskInstanceId 任务实例ID
	 * @return 扩展属性
	 */
        Map<String, String> getTaskInstanceExtendAttrs(String engineTaskInstanceId);

        /**
         * Get task name by given define ID.
         * @param processDefinitionKey  process definition key
         * @param taskDefineId task define ID
         * @return task name
         */
        String getTaskNameByDefineId(String processDefinitionKey, String taskDefineId);

        /**
         * Get process instance's variables
         * @param engineProcessInstanceId process instance ID(NOT NULL)
         * @return process instance's variables
         */
        Set<String> getProcessInstanceVariableNames(String engineProcessInstanceId);
	
	/**
	 * 放弃执行这个任务。
	 * <p>
	 * 一般流程引擎（GWFP引擎）将不会自动向下执行流程,具体请参考流程引擎的介绍文档。<br>
	 * @param engineTaskInstanceId
	 */
	void abortTaskInstance(String engineTaskInstanceId) throws ProcessException;
	
	/**
	 * 根据任务ID取得任务执行人角色
	 * @param engineTaskInstanceId 任务ID
	 * @return 任务执行人角色
	 * @throws ProcessException 流程异常
	 */
	String obtainTaskRole(String engineTaskInstanceId) throws ProcessException;
	
	/**
	 * <p> 委托或重新指定任务执行人
	* @Title: reassignActivityPerformer
	* @Description: 委托或重新指定任务执行人
	* @param  engineProcessInstanceId  流程ID
	* @param  engineTaskInstanceId   任务ID
	* @param  srcPerformer  原执行人
	* @param  newPerformer  新执行人
	* @throws ProcessException    
	* @return void    返回类型
	* 设定文件
	 */
	void reassignActivityPerformer(String engineProcessInstanceId,String engineTaskInstanceId,String srcPerformer,String newPerformer) throws ProcessException;

        // Comment by MENGRAN
//	ProcessView getProcessByProcessId(String gwfpProcessId) throws ProcessException;
	
//	ProcessStatus getProcessStateFromGWFP(String GWFPProcessInstanceId) throws RuntimeProcessException;
	
//	/**
//	 * 终止工作流中的流程
//	 * @param processInstanceId
//	 * @param operator
//	 * @param reason
//	 * @throws ProcessException
//	 */
//	void terminalProcessOfGWFP(String processInstanceId, String operator, String reason) throws ProcessException;
	
}
