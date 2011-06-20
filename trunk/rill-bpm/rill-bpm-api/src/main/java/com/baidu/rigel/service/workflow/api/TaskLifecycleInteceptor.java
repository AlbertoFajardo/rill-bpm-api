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
package com.baidu.rigel.service.workflow.api;

import java.util.Map;

import com.baidu.rigel.service.workflow.api.exception.ProcessException;
import com.baidu.rigel.service.workflow.api.exception.TaskInitialException;

/**
 * Task lifecycle interceptor. Defines {@link #init(java.lang.Object) Initialize}, {@link #preComplete(java.lang.Object) Pre-complete},
 * {@link #postComplete(java.lang.Object) Post-complete}, {@link #afterComplete(java.lang.Object) After-complete} extension points.
 * <p>
 * 	This is core extension point of work-flow package, we also provide some out-of-box interceptors.
 * <p>
 * 	For example: Complete Task[A] and generated Task[B],Task[C]<br>
 * 	Extension point invoke sequence like this:
 * 	<ul>
 * 		<li>A'{@link #preComplete(Object)}
 * 		<li>A'{@link #postComplete(Object)}
 * 		<li>B'{@link #init(Object)}
 * 		<li>C'{@link #init(Object)}
 * 		<li>A'{@link #afterComplete(Object)}
 * @author mengran
 * 
 **/
public interface TaskLifecycleInteceptor {

    /**
     * Task initialize lifecycle interceptor
     * 
     * @param taskContext task execution context
     * @exception TaskInitialException Exception occurred when operation
     */
    void init(Object taskContext) throws TaskInitialException;

    /**
     * Task pre-complete lifecycle interceptor
     * @param taskContext task execution context
     * @return workflow parameters MAP
     * @throws ProcessException Exception occurred when operation
     */
    Map<String, Object> preComplete(Object taskContext) throws ProcessException;

    /**
     * Task post-complete lifecycle interceptor
     * @param taskContext task execution context
     * @throws ProcessException Exception occurred when operation
     */
    void postComplete(Object taskContext) throws ProcessException;

    /**
     * Task after-complete lifecycle interceptor 
     * @param taskContext task execution context
     * @throws ProcessException Exception occurred when operation
     */
    void afterComplete(Object taskContext) throws ProcessException;

    /**
     * Exception handler method that occurred.
     * <B>Not allow throw any exception</B>
     * 
     * <p>
     * 	<ul><li>If method only want to handle exception throwed by self then add check as <code>if (this == exceptionMurderer)</code>
     * @param e original exception
     * @param exceptionMurderer exception murderer
     */
    void onExceptionOccurred(Exception e, TaskLifecycleInteceptor exceptionMurderer);
}
