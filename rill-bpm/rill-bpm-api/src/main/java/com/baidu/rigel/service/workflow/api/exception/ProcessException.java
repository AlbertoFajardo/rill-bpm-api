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
package com.baidu.rigel.service.workflow.api.exception;

/**
 * Workflow Exception.
 * 
 * @author mengran
 */
public class ProcessException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -3058046252123750658L;
    private String engineProcessInstanceId;
    private String engineTaskInstanceId;
    private TASK_LIFECYCLE_PHASE taskLifecycleInterceptorPhase;
    private PROCESS_PHASE processInterceptorPhase;
    private String boId;
    private String operator;
    
    public static enum PROCESS_PHASE {
        UNKNOWN, BEFORE_CREATE, POST_CREATE, ENGINE_OPERATION, BEFORE_OPERATION, POST_OPERATION
    }
    public static enum TASK_LIFECYCLE_PHASE {
        UNKNOWN, INIT, PRE_COMPLETE, POST_COMPLETE, AFTER_COMPLETE, ENGINE_OPERATION
    }

    public String getOperator() {
        return operator;
    }

    public ProcessException setOperator(String operator) {
        this.operator = operator;
        return this;
    }

    public String getBoId() {
        return boId;
    }

    public ProcessException setBoId(String boId) {
        this.boId = boId;
        return this;
    }

    public PROCESS_PHASE getProcessInterceptorPhase() {
        return processInterceptorPhase == null ? PROCESS_PHASE.UNKNOWN : processInterceptorPhase;
    }

    public ProcessException setProcessInterceptorPhase(PROCESS_PHASE processInterceptorPhase) {
        this.processInterceptorPhase = processInterceptorPhase;
        return this;
    }

    public String getEngineProcessInstanceId() {
        return engineProcessInstanceId;
    }

    public ProcessException setEngineProcessInstanceId(String engineProcessInstanceId) {
        this.engineProcessInstanceId = engineProcessInstanceId;
        return this;
    }

    public String getEngineTaskInstanceId() {
        return engineTaskInstanceId;
    }

    public ProcessException setEngineTaskInstanceId(String engineTaskInstanceId) {
        this.engineTaskInstanceId = engineTaskInstanceId;
        return this;
    }

    public TASK_LIFECYCLE_PHASE getTaskLifecycleInterceptorPhase() {
        return taskLifecycleInterceptorPhase == null ? TASK_LIFECYCLE_PHASE.UNKNOWN : taskLifecycleInterceptorPhase;
    }

    public ProcessException setTaskLifecycleInterceptorPhase(TASK_LIFECYCLE_PHASE taskLifecycleInterceptorPhase) {
        this.taskLifecycleInterceptorPhase = taskLifecycleInterceptorPhase;
        return this;
    }

    /**
     * 
     */
    public ProcessException() {
    }

    /**
     * @param msg
     */
    public ProcessException(String msg) {
        super(msg);
    }

    /**
     * @param cause
     */
    public ProcessException(Throwable cause) {
        super(cause);
    }

    /**
     * @param msg
     * @param cause
     */
    public ProcessException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * @param e
     */
    public ProcessException(Exception e) {
        super(e.getMessage(), e);
    }
}
