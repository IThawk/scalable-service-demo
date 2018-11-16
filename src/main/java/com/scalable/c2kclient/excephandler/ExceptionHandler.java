package com.scalable.c2kclient.excephandler;

public interface ExceptionHandler {
	public boolean support(Throwable t);

	public void handle(Throwable t, String message);
}