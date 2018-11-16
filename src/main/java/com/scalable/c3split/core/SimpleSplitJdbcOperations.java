package com.scalable.c3split.core;

import java.util.List;

/**提供了一个新的更加上层、抽象的SimpleSplitJdbcOperations类，这个类的操作不针对sql，使用者不需要知道sql，也不用操作sql，而是直接把java的领域对象模型传入即可，
 * 所有sql的生成、分片的确定都由dbsplit框架来实现。
 * @author Administrator
 *
 */
public interface SimpleSplitJdbcOperations extends SplitJdbcOperations {

	public <K, T> void insert(K splitKey, T bean);

	public <K, T> void update(K splitKey, T bean);

	public <K, T> void delete(K splitKey, long id, Class<T> clazz);

	public <K, T> T get(K splitKey, long id, final Class<T> clazz);

	public <K, T> T get(K splitKey, String key, String value,
			final Class<T> clazz);

	public <K, T> List<T> search(K splitKey, T bean);

	public <K, T> List<T> search(K splitKey, T bean, String name,
			Object valueFrom, Object valueTo);

	public <K, T> List<T> search(K splitKey, T bean, String name, Object value);
}