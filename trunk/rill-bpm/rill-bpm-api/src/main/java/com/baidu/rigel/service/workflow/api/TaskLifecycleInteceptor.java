package com.baidu.rigel.service.workflow.api;

import java.util.Map;

import com.baidu.rigel.service.workflow.api.exception.ProcessException;
import com.baidu.rigel.service.workflow.api.exception.TaskInitialException;


/**
 * 公司名：百度 <br>
 * 系统名：Rigel支持系统<br>
 * 子系统名: SP Platform<br>
 * 模块名：Work flow <br>
 * 文件名：TaskLifecycleInteceptor.java<br>
 * 功能说明: 任务生命周期组件。定义了任务初始化、任务完成前置、任务完成后置、任务完成后四个扩展点。<br>
 * <p>
 * 	这个是非常重要的工作流扩展点，我们也提供了一些开箱即用的扩展点。
 * <p>
 * 	举一个具体的例子：任务A完成后产生任务B、任务C<br>
 * 	那么生命周期执行的顺序为：
 * 	<ul>
 * 		<li>A的{@link #preComplete(Object)}
 * 		<li>A的{@link #postComplete(Object)}
 * 		<li>B的{@link #init(Object)}
 * 		<li>C的{@link #init(Object)}
 * 		<li>A的{@link #afterComplete(Object)}
 * <p>
 * 	V1.1新增接口{{@link #onExceptionOccurred(Exception, TaskLifecycleInteceptor)}<br>
 * 	这个接口的意义：在当前参与工作流操作的任务生命周期任一组件调用过程中抛出异常后,接收异常通知。
 * 
 * @see com.baidu.rigel.sp.platform.workflow.integration.gwfp.GWFPTaskCommonCheckInteceptor
 * @see com.baidu.rigel.sp.platform.workflow.integration.gwfp.GWFPTaskRelatedDataPersistInteceptor
 * @author mengran
 * @version 1.1
 * @date 2010-7-2
 * @date 2011-01-05
**/
public interface TaskLifecycleInteceptor {

	/**
	 * 此方法抛出TaskInitialException，因为此方法调用时机为流程创建后/任务结束后，<br>
	 * 	此时前面一个事务体应该是正确提交的，所以应该在事务管理器中配置此异常为非回滚。
	 * @param taskContext 任务执行上下文
	 */
	void init(Object taskContext) throws TaskInitialException;
	
	/**
	 * 任务完成前扩展点。这里可以扩展来做很多工作，当然也有一部分工作（任务的Common check）已经做了。
	 * @param taskContext 任务执行上下文
	 * @return 工作流变量的Map
	 * @throws ProcessException 工作流异常
	 */
	Map<String, Object> preComplete(Object taskContext) throws ProcessException;
	
	/**
	 * 任务完成后扩展点。在某些存在分布式事务的问题的工作流集成系统中，这里可能会引发问题。
	 * @param taskContext 任务执行上下文
	 * @throws ProcessException 后置扩展点抛出的所有可能异常。由于会回滚业务事务，可能会引发分布式事务问题。
	 */
	void postComplete(Object taskContext) throws ProcessException;
	
	
	/**
	 * 任务完成后扩展点。这个callback执行时刻和{{@link #postComplete(Object)}时刻不同。
	 * <p>
	 * 	假设A任务完成后会生成B，C两个任务。那么这个callback的执行时刻为A任务处理过程结束前。<br>
	 * 	以这个例子还说就是B，C任务的{{@link #init(Object)}后。
	 * @param taskContext 任务执行上下文
	 * @throws ProcessException 扩展点抛出的所有可能异常。由于会回滚业务事务，可能会引发分布式事务问题。
	 */
	void afterComplete(Object taskContext) throws ProcessException;
	
	/**
	 * 当任务生命周期方法调用抛出Exception时的回调方法。<br>
	 * <B>此方法不应该抛出任何异常。</B>
	 * 
	 * <p>
	 * 	这个参数的使用意义：
	 * 		<ul><li>如果当前组件只关心自己的方法调用抛出的异常，那么应该有限制条件<code>if (this == exceptionMurderer)</code>
	 * @param e 异常
	 * @param exceptionMurderer 出现异常的元凶。
	 */
	void onExceptionOccurred(Exception e, TaskLifecycleInteceptor exceptionMurderer);
	
}
