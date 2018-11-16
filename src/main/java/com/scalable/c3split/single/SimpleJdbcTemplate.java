package com.scalable.c3split.single;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.scalable.c3split.core.sql.util.OrmUtil;
import com.scalable.c3split.core.sql.util.SqlUtil;

/**这个包并不是用来分库分表的，而是用来扩展Spring JdbcTemplate接口api的，提供了更高层次的抽象的api，可以直接对一个java bean进行增删改查，可以算作一个简单易用的小的orm框架的实现。
 * @author Administrator
 *
 */
public class SimpleJdbcTemplate extends JdbcTemplate implements SimpleJdbcOperations {

	private static final Logger log = LoggerFactory.getLogger(SimpleJdbcTemplate.class);

	public <T> void insert(T bean) {
		SqlUtil.SqlRunningBean srb = SqlUtil.generateInsertSql(bean);
		log.debug("Insert, the bean: {} ---> the SQL: {} ---> the params: {}", bean, srb.getSql(), srb.getParams());
		this.update(srb.getSql(), srb.getParams());
	}

	public <T> void update(T bean) {
		SqlUtil.SqlRunningBean srb = SqlUtil.generateUpdateSql(bean);
		log.debug("Update, the bean: {} ---> the SQL: {} ---> the params: {}", bean, srb.getSql(), srb.getParams());
		this.update(srb.getSql(), srb.getParams());
	}

	public <T> void delete(long id, Class<T> clazz) {
		SqlUtil.SqlRunningBean srb = SqlUtil.generateDeleteSql(id, clazz);
		log.debug("Delete, the bean: {} ---> the SQL: {} ---> the params: {}", id, srb.getSql(), srb.getParams());
		this.update(srb.getSql(), srb.getParams());
	}

	public <T> T get(long id, final Class<T> clazz) {
		SqlUtil.SqlRunningBean srb = SqlUtil.generateSelectSql("id", id, clazz);

		T bean = this.queryForObject(srb.getSql(), srb.getParams(), new RowMapper<T>() {
			public T mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				return OrmUtil.convertRow2Bean(rs, clazz);
			}
		});
		return bean;
	}

	public <T> T get(String name, Object value, final Class<T> clazz) {
		SqlUtil.SqlRunningBean srb = SqlUtil.generateSelectSql(name, value, clazz);

		T bean = this.queryForObject(srb.getSql(), srb.getParams(), new RowMapper<T>() {
			public T mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				return OrmUtil.convertRow2Bean(rs, clazz);
			}
		});
		return bean;
	}

	public <T> List<T> search(final T bean) {
		SqlUtil.SqlRunningBean srb = SqlUtil.generateSearchSql(bean);

		List<T> beans = this.query(srb.getSql(), srb.getParams(), new RowMapper<T>() {
			public T mapRow(ResultSet rs, int rowNum) throws SQLException {
				return (T) OrmUtil.convertRow2Bean(rs, bean.getClass());
			}
		});
		return beans;
	}

	public <T> List<T> search(String sql, Object[] params, final Class<T> clazz) {
		List<T> beans = this.query(sql, params, new RowMapper<T>() {
			public T mapRow(ResultSet rs, int rowNum) throws SQLException {
				return (T) OrmUtil.convertRow2Bean(rs, clazz);
			}
		});
		return beans;
	}
}