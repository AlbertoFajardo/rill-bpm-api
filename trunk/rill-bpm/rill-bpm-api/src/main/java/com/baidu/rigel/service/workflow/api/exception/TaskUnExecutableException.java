package com.baidu.rigel.service.workflow.api.exception;

public class TaskUnExecutableException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 2659089594876889389L;

	public TaskUnExecutableException() {
		super();
	}

	public TaskUnExecutableException(String message, Throwable cause) {
		super(message, cause);
	}

	public TaskUnExecutableException(String message) {
		super(message);
	}

	public TaskUnExecutableException(Throwable cause) {
		super(cause);
	}



}
