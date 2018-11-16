package com.scalable.c2kclient.handlers;

/**用于把消息转换成不同类型的对象，例如json对象，bean对象，然后把这个类型传递给子类进行处理。
 * @author DELL
 *
 */
public interface MessageHandler {

	public void execute(String message);

}
