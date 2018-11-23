package com.scalable.c4redic.strategy;

public interface SelectStrategy {

	/**在读写分离的场景下用来选择从哪个从节点读取数据
	 * @param count
	 * @return
	 */
	public int select(int count);

}