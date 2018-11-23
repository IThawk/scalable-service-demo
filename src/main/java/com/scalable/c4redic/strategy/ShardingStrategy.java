package com.scalable.c4redic.strategy;

public interface ShardingStrategy {

	/**用来路由分片策略的实现
	 * @param key
	 * @param nodeCount
	 * @return
	 */
	public <T> int key2node(T key, int nodeCount);

}