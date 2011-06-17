/**
 * 
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
