package com.scalable.c3split.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.scalable.c3split.core.sql.parser.SplitSqlParser;
import com.scalable.c3split.core.sql.parser.SplitSqlStructure;
import com.scalable.c3split.core.sql.util.OrmUtil;
import com.scalable.c3split.excep.NotSupportedException;

/**实现了SplitJdbcOperations接口，里面包含了类似于Spring JdbcTemplate的api，包含了增删改查操作，唯一不同的是，每个方法都要传入一个分区主键。
 * @author Administrator
 *
 */
public class SplitJdbcTemplate implements SplitJdbcOperations {

	protected static final Logger log = LoggerFactory.getLogger(SplitJdbcTemplate.class);

	protected SplitTablesHolder splitTablesHolder;

	protected SplitActionRunner splitActionRunner = new SplitActionRunner();

	protected boolean readWriteSeparate = false;

	interface SplitAction<T> {
		T doSplitAction(JdbcTemplate jt, String sql);
	}

	class SplitActionRunner {
		<T, K> T runSplitAction(K splitKey, String sql, SplitAction<T> splitAction) {
			log.debug("runSplitAction entry, splitKey {} sql {}", splitKey, sql);

			SplitSqlStructure splitSqlStructure = SplitSqlParser.INST.parseSplitSql(sql);

			String dbName = splitSqlStructure.getDbName();
			String tableName = splitSqlStructure.getTableName();

			SplitTable splitTable = splitTablesHolder.searchSplitTable(dbName, tableName);

			SplitStrategy splitStrategy = splitTable.getSplitStrategy();

			int dbNo = splitStrategy.getDbNo(splitKey);
			int tableNo = splitStrategy.getTableNo(splitKey);

			List<SplitDb> splitDbs = splitTable.getSplitDbs();

			SplitDb sn = splitDbs.get(dbNo);
			JdbcTemplate jt = getJdbcTemplate(sn, false);

			sql = splitSqlStructure.getSplitSql(dbNo, tableNo);

			log.debug("runSplitAction do action, splitKey {} sql {} dbName {} tableName {} dbNo {} tableNo {}", splitKey, sql, dbName, tableName, dbNo, tableNo);
			T result = splitAction.doSplitAction(jt, sql);

			log.debug("runSplitAction return, {} are returned, splitKey {} sql {}", result, splitKey, sql);
			return result;
		}
	}

	public SplitJdbcTemplate() {

	}

	public SplitJdbcTemplate(SplitTablesHolder splitTablesHolder) {
		this.splitTablesHolder = splitTablesHolder;
	}

	public SplitJdbcTemplate(List<String> ipPorts, String user, String password, String... tables) {
		this.addTable(ipPorts, user, password, tables);
	}

	public void addTable(List<String> ipPorts, String user, String password, String... tables) {
		// TODO parse datasources and tables
	}

	public <T, K> T execute(K splitKey, ConnectionCallback<T> action) throws DataAccessException {
		throw new NotSupportedException();
	}

	public <T, K> T execute(K splitKey, StatementCallback<T> action) throws DataAccessException {
		throw new NotSupportedException();
	}

	public <K> void execute(K splitKey, String sql) throws DataAccessException {
		throw new NotSupportedException();
	}

	public <T, K> T query(K splitKey, String sql, ResultSetExtractor<T> rse) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<T>() {
			public T doSplitAction(JdbcTemplate jt, String sql) {
				T result = jt.query(sql, rse);
				return result;
			}
		});
	}

	public <K> void query(K splitKey, String sql, RowCallbackHandler rch) throws DataAccessException {
		splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<Object>() {
			public Object doSplitAction(JdbcTemplate jt, String sql) {
				jt.query(sql, rch);

				return null;
			}
		});
	}

	public <T, K> List<T> query(K splitKey, String sql, RowMapper<T> rowMapper) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<List<T>>() {
			public List<T> doSplitAction(JdbcTemplate jt, String sql) {
				List<T> result = jt.query(sql, rowMapper);
				return result;
			}
		});
	}

	public <T, K> T queryForObject(K splitKey, String sql, RowMapper<T> rowMapper) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<T>() {
			public T doSplitAction(JdbcTemplate jt, String sql) {
				T result = jt.queryForObject(sql, rowMapper);
				return result;
			}
		});
	}

	public <T, K> T queryForObject(K splitKey, String sql, Class<T> requiredType) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<T>() {
			public T doSplitAction(JdbcTemplate jt, String sql) {
				T result = jt.queryForObject(sql, requiredType);
				return result;
			}
		});
	}

	public <K> Map<String, Object> queryForMap(K splitKey, String sql) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<Map<String, Object>>() {
			public Map<String, Object> doSplitAction(JdbcTemplate jt, String sql) {
				Map<String, Object> result = jt.queryForMap(sql);
				return result;
			}
		});
	}

	public <T, K> List<T> queryForList(K splitKey, String sql, Class<T> elementType) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<List<T>>() {
			public List<T> doSplitAction(JdbcTemplate jt, String sql) {
				List<T> result = jt.queryForList(sql, elementType);
				return result;
			}
		});
	}

	public <K> List<Map<String, Object>> queryForList(K splitKey, String sql) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<List<Map<String, Object>>>() {
			public List<Map<String, Object>> doSplitAction(JdbcTemplate jt, String sql) {
				List<Map<String, Object>> result = jt.queryForList(sql);
				return result;
			}
		});
	}

	public <K> SqlRowSet queryForRowSet(K splitKey, String sql) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<SqlRowSet>() {
			public SqlRowSet doSplitAction(JdbcTemplate jt, String sql) {
				SqlRowSet result = jt.queryForRowSet(sql);
				return result;
			}
		});
	}

	public <K> int update(K splitKey, String sql) throws DataAccessException {
		throw new NotSupportedException();
	}

	public <K> int[] batchUpdate(K splitKey, String... sql) throws DataAccessException {
		throw new NotSupportedException();
	}

	public <T, K> T execute(K splitKey, PreparedStatementCreator psc, PreparedStatementCallback<T> action) throws DataAccessException {
		throw new NotSupportedException();
	}

	public <T, K> T execute(K splitKey, String sql, PreparedStatementCallback<T> action) throws DataAccessException {
		throw new NotSupportedException();
	}

	public <T, K> T query(K splitKey, PreparedStatementCreator psc, ResultSetExtractor<T> rse) throws DataAccessException {
		throw new NotSupportedException();
	}

	public <T, K> T query(K splitKey, String sql, PreparedStatementSetter pss, ResultSetExtractor<T> rse) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<T>() {
			public T doSplitAction(JdbcTemplate jt, String sql) {
				T result = jt.query(sql, pss, rse);
				return result;
			}
		});
	}

	public <T, K> T query(K splitKey, String sql, Object[] args, int[] argTypes, ResultSetExtractor<T> rse) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<T>() {
			public T doSplitAction(JdbcTemplate jt, String sql) {
				T result = jt.query(sql, args, argTypes, rse);
				return result;
			}
		});
	}

	public <T, K> T query(K splitKey, String sql, Object[] args, ResultSetExtractor<T> rse) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<T>() {
			public T doSplitAction(JdbcTemplate jt, String sql) {
				T result = jt.query(sql, args, rse);
				return result;
			}
		});
	}

	public <T, K> T query(K splitKey, String sql, ResultSetExtractor<T> rse, Object... args) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<T>() {
			public T doSplitAction(JdbcTemplate jt, String sql) {
				T result = jt.query(sql, rse, args);
				return result;
			}
		});
	}

	public <K> void query(K splitKey, PreparedStatementCreator psc, RowCallbackHandler rch) throws DataAccessException {
		throw new NotSupportedException();
	}

	public <K> void query(K splitKey, String sql, PreparedStatementSetter pss, RowCallbackHandler rch) throws DataAccessException {
		splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<Object>() {
			public Object doSplitAction(JdbcTemplate jt, String sql) {
				jt.query(sql, pss, rch);
				return null;
			}
		});
	}

	public <K> void query(K splitKey, String sql, Object[] args, int[] argTypes, RowCallbackHandler rch) throws DataAccessException {
		splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<Object>() {
			public Object doSplitAction(JdbcTemplate jt, String sql) {
				jt.query(sql, args, argTypes, rch);
				return null;
			}
		});
	}

	public <K> void query(K splitKey, String sql, Object[] args, RowCallbackHandler rch) throws DataAccessException {
		splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<Object>() {
			public Object doSplitAction(JdbcTemplate jt, String sql) {
				jt.query(sql, args, rch);
				return null;
			}
		});
	}

	public <K> void query(K splitKey, String sql, RowCallbackHandler rch, Object... args) throws DataAccessException {
		splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<Object>() {
			public Object doSplitAction(JdbcTemplate jt, String sql) {
				jt.query(sql, rch, args);
				return null;
			}
		});
	}

	public <T, K> List<T> query(K splitKey, PreparedStatementCreator psc, RowMapper<T> rowMapper) throws DataAccessException {
		throw new NotSupportedException();
	}

	public <T, K> List<T> query(K splitKey, String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<List<T>>() {
			public List<T> doSplitAction(JdbcTemplate jt, String sql) {
				List<T> ret = jt.query(sql, pss, rowMapper);
				return ret;
			}
		});
	}

	public <T, K> List<T> query(K splitKey, String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<List<T>>() {
			public List<T> doSplitAction(JdbcTemplate jt, String sql) {
				List<T> ret = jt.query(sql, args, argTypes, rowMapper);
				return ret;
			}
		});
	}

	public <T, K> List<T> query(K splitKey, String sql, Object[] args, Class<T> requiredType) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<List<T>>() {
			public List<T> doSplitAction(JdbcTemplate jt, String sql) {
				List<T> ret = jt.query(sql, args, new RowMapper<T>() {
					public T mapRow(ResultSet rs, int rowNum) throws SQLException {
						return OrmUtil.convertRow2Bean(rs, requiredType);
					}
				});
				return ret;
			}
		});
	}

	public <T, K> List<T> query(K splitKey, String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<List<T>>() {
			public List<T> doSplitAction(JdbcTemplate jt, String sql) {
				List<T> ret = jt.query(sql, rowMapper, args);
				return ret;
			}
		});
	}

	public <T, K> T queryForObject(K splitKey, String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<T>() {
			public T doSplitAction(JdbcTemplate jt, String sql) {
				T ret = jt.queryForObject(sql, args, argTypes, rowMapper);
				return ret;
			}
		});
	}

	public <T, K> T queryForObject(K splitKey, String sql, Object[] args, RowMapper<T> rowMapper) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<T>() {
			public T doSplitAction(JdbcTemplate jt, String sql) {
				T ret = jt.queryForObject(sql, args, rowMapper);
				return ret;
			}
		});
	}

	public <T, K> T queryForObject(K splitKey, String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<T>() {
			public T doSplitAction(JdbcTemplate jt, String sql) {
				T ret = jt.queryForObject(sql, rowMapper, args);
				return ret;
			}
		});
	}

	public <T, K> T queryForObject(K splitKey, String sql, Object[] args, int[] argTypes, Class<T> requiredType) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<T>() {
			public T doSplitAction(JdbcTemplate jt, String sql) {
				T ret = jt.queryForObject(sql, args, argTypes, requiredType);
				return ret;
			}
		});
	}

	public <T, K> T queryForObject(K splitKey, String sql, Object[] args, Class<T> requiredType) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<T>() {
			public T doSplitAction(JdbcTemplate jt, String sql) {
				T ret = jt.queryForObject(sql, args, requiredType);
				return ret;
			}
		});
	}

	public <T, K> T queryForObject(K splitKey, String sql, Class<T> requiredType, Object... args) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<T>() {
			public T doSplitAction(JdbcTemplate jt, String sql) {
				T ret = jt.queryForObject(sql, requiredType, args);
				return ret;
			}
		});
	}

	public <K> Map<String, Object> queryForMap(K splitKey, String sql, Object[] args, int[] argTypes) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<Map<String, Object>>() {
			public Map<String, Object> doSplitAction(JdbcTemplate jt, String sql) {
				Map<String, Object> ret = jt.queryForMap(sql, args, argTypes);
				return ret;
			}
		});
	}

	public <K> Map<String, Object> queryForMap(K splitKey, String sql, Object... args) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<Map<String, Object>>() {
			public Map<String, Object> doSplitAction(JdbcTemplate jt, String sql) {
				Map<String, Object> ret = jt.queryForMap(sql, args);
				return ret;
			}
		});
	}

	public <T, K> List<T> queryForList(K splitKey, String sql, Object[] args, int[] argTypes, Class<T> elementType) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<List<T>>() {
			public List<T> doSplitAction(JdbcTemplate jt, String sql) {
				List<T> ret = jt.queryForList(sql, args, argTypes, elementType);
				return ret;
			}
		});
	}

	/*
	 * 查询数据库中一列多行数据，即查询数据库中单列数据存入一个list中，方式如下：（预处理sql，需要注入参数）
	 * 注意：这个T虽然是泛型，但是只支持Integer.class String.class 这种单数据类型的，自己定义的Bean不支持。（所以用来查询单列数据,同前面一个意思，后面不再解释）
	 */
	public <T, K> List<T> queryForList(K splitKey, String sql, Object[] args, Class<T> elementType) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<List<T>>() {
			public List<T> doSplitAction(JdbcTemplate jt, String sql) {
				List<T> ret = jt.queryForList(sql, args, elementType);
				return ret;
			}
		});
	}

	public <T, K> List<T> queryForList(K splitKey, String sql, Class<T> elementType, Object... args) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<List<T>>() {
			public List<T> doSplitAction(JdbcTemplate jt, String sql) {
				List<T> ret = jt.queryForList(sql, elementType, args);
				return ret;
			}
		});
	}

	public <K> List<Map<String, Object>> queryForList(K splitKey, String sql, Object[] args, int[] argTypes) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<List<Map<String, Object>>>() {
			public List<Map<String, Object>> doSplitAction(JdbcTemplate jt, String sql) {
				List<Map<String, Object>> ret = jt.queryForList(sql, args, argTypes);
				return ret;
			}
		});
	}

	public <K> List<Map<String, Object>> queryForList(K splitKey, String sql, Object... args) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<List<Map<String, Object>>>() {
			public List<Map<String, Object>> doSplitAction(
					JdbcTemplate jt, String sql) {
				List<Map<String, Object>> ret = jt.queryForList(sql,
						args);
				return ret;
			}
		});
	}

	public <K> SqlRowSet queryForRowSet(K splitKey, String sql, Object[] args, int[] argTypes) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<SqlRowSet>() {
			public SqlRowSet doSplitAction(JdbcTemplate jt, String sql) {
				SqlRowSet ret = jt.queryForRowSet(sql, args, argTypes);
				return ret;
			}
		});
	}

	public <K> SqlRowSet queryForRowSet(K splitKey, String sql, Object... args) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<SqlRowSet>() {
			public SqlRowSet doSplitAction(JdbcTemplate jt, String sql) {
				SqlRowSet ret = jt.queryForRowSet(sql, args);
				return ret;
			}
		});
	}

	public <K> int update(K splitKey, PreparedStatementCreator psc) throws DataAccessException {
		throw new NotSupportedException();
	}

	public <K> int update(K splitKey, PreparedStatementCreator psc, KeyHolder generatedKeyHolder) throws DataAccessException {
		throw new NotSupportedException();
	}

	public <K> int update(K splitKey, String sql, PreparedStatementSetter pss) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<Integer>() {
			public Integer doSplitAction(JdbcTemplate jt, String sql) {
				Integer ret = jt.update(sql, pss);
				return ret;
			}
		});
	}

	public <K> int update(K splitKey, String sql, Object[] args, int[] argTypes) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<Integer>() {
			public Integer doSplitAction(JdbcTemplate jt, String sql) {
				Integer ret = jt.update(sql, args, argTypes);
				return ret;
			}
		});
	}

	public <K> int update(K splitKey, String sql, Object... args) throws DataAccessException {
		return splitActionRunner.runSplitAction(splitKey, sql, new SplitAction<Integer>() {
			public Integer doSplitAction(JdbcTemplate jt, String sql) {
				Integer ret = jt.update(sql, args);
				return ret;
			}
		});
	}

	public <K> int[] batchUpdate(K splitKey, String sql, BatchPreparedStatementSetter pss) throws DataAccessException {
		throw new NotSupportedException();
	}

	public <K> int[] batchUpdate(K splitKey, String sql, List<Object[]> batchArgs) throws DataAccessException {
		throw new NotSupportedException();
	}

	public <K> int[] batchUpdate(K splitKey, String sql, List<Object[]> batchArgs, int[] argTypes) throws DataAccessException {
		throw new NotSupportedException();
	}

	public <T, K> int[][] batchUpdate(K splitKey, String sql, Collection<T> batchArgs, int batchSize, ParameterizedPreparedStatementSetter<T> pss) throws DataAccessException {
		throw new NotSupportedException();
	}

	public <T, K> T execute(K splitKey, CallableStatementCreator csc,
			CallableStatementCallback<T> action) throws DataAccessException {
		throw new NotSupportedException();
	}

	public <T, K> T execute(K splitKey, String callString, CallableStatementCallback<T> action) throws DataAccessException {
		throw new NotSupportedException();
	}

	public <K> Map<String, Object> call(K splitKey, CallableStatementCreator csc, List<SqlParameter> declaredParameters) throws DataAccessException {
		throw new NotSupportedException();
	}

	public SplitTablesHolder getSplitTablesHolder() {
		return splitTablesHolder;
	}

	public void setSplitTablesHolder(SplitTablesHolder splitTablesHolder) {
		this.splitTablesHolder = splitTablesHolder;
	}

	public boolean isReadWriteSeparate() {
		return readWriteSeparate;
	}

	public void setReadWriteSeparate(boolean readWriteSeparate) {
		this.readWriteSeparate = readWriteSeparate;
	}

	protected JdbcTemplate getWriteJdbcTemplate(SplitDb sn) {
		return getJdbcTemplate(sn, false);
	}

	protected JdbcTemplate getReadJdbcTemplate(SplitDb sn) {
		return getJdbcTemplate(sn, true);
	}

	protected JdbcTemplate getJdbcTemplate(SplitDb sn, boolean read) {
		if (!read)
			return sn.getMasterTemplate();

		if (readWriteSeparate)
			return sn.getRoundRobinSlaveTempate();

		return sn.getMasterTemplate();
	}
}