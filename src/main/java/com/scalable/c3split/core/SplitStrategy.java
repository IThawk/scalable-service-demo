package com.scalable.c3split.core;

/**获取某个分区主键对应的数据库实例下标、数据库下标、和节点下标
 * @author DELL
 *
 */
public interface SplitStrategy {

	public <K> int getDbNo(K splitKey);

	public <K> int getTableNo(K splitKey);

}
