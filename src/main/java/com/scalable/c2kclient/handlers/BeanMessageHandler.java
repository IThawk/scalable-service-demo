package com.scalable.c2kclient.handlers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cdel.util.helper.JacksonUtil;
import com.scalable.c2kclient.excephandler.ExceptionHandler;

/**把一个json字符串转换成一个领域对象，然后将这个领域对象模型传递给子类进行处理
 * @author DELL
 *
 * @param <T>
 */
public abstract class BeanMessageHandler<T> extends SafelyMessageHandler {

	protected static Logger log = LoggerFactory.getLogger(BeanMessageHandler.class);

	private Class<T> clazz;

	public BeanMessageHandler(Class<T> clazz) {
		super();
		this.clazz = clazz;
	}

	public BeanMessageHandler(Class<T> clazz, ExceptionHandler excepHandler) {
		super(excepHandler);
		this.clazz = clazz;
	}

	public BeanMessageHandler(Class<T> clazz, List<ExceptionHandler> excepHandlers) {
		super(excepHandlers);
		this.clazz = clazz;
	}

	protected void doExecute(String message) throws Exception {
		T bean = JacksonUtil.jsonToObject(message, clazz);
		doExecuteBean(bean);
	}

	protected abstract void doExecuteBean(T bean);

}