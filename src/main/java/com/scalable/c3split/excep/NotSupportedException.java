package com.scalable.c3split.excep;

public class NotSupportedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NotSupportedException() {
		super("Api not supported by dbsplit framework.");
	}

	public NotSupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotSupportedException(String message) {
		super(message);
	}

	public NotSupportedException(Throwable cause) {
		super(cause);
	}

}