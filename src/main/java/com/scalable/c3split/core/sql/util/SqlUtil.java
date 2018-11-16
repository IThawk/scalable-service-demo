package com.scalable.c3split.core.sql.util;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.springframework.util.StringUtils;

import com.scalable.c3split.util.reflect.FieldHandler;
import com.scalable.c3split.util.reflect.FieldVisitor;
import com.scalable.c3split.util.reflect.ReflectionUtil;

/**通过实体类生成sql语句。
 * 包含了生成插入、更新、删除、和查询sql的具体实现。
 * @author DELL
 *
 */
public abstract class SqlUtil {

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	@ToString
	public static class SqlRunningBean {
		private String sql; // 分片后的sql语句
		private Object[] params; // 执行sql所需要的参数信息，这些信息会被传递给SimpleSplitJdbcTemplate，在确定具体的分片所使用的数据源后，发给数据库执行
	}

	public static <T> SqlRunningBean generateInsertSql(T bean, String databasePrefix, String tablePrefix, int databseIndex, int tableIndex) {
		final StringBuilder sb = new StringBuilder();
		sb.append("insert into ");

		if (StringUtils.isEmpty(tablePrefix))
			tablePrefix = OrmUtil.javaClassName2DbTableName(bean.getClass().getSimpleName());

		sb.append(getQualifiedTableName(databasePrefix, tablePrefix, databseIndex, tableIndex));

		sb.append("(");

		final List<Object> params = new LinkedList<Object>();

		new FieldVisitor<T>(bean).visit(new FieldHandler() {
			public void handle(int index, Field field, Object value) {
				if (index != 0)
					sb.append(",");

				sb.append(OrmUtil.javaFieldName2DbFieldName(field.getName()));

				if (value instanceof Enum)
					value = ((Enum<?>) value).ordinal();

				params.add(value);
			}
		});

		sb.append(") values (");
		sb.append(OrmUtil.generateParamPlaceholders(params.size()));
		sb.append(")");

		return new SqlRunningBean(sb.toString(), params.toArray());
	}

	public static <T> SqlRunningBean generateInsertSql(T bean) {
		return generateInsertSql(bean, null, null, -1, -1);
	}

	public static <T> SqlRunningBean generateInsertSql(T bean, String databasePrefix) {
		return generateInsertSql(bean, databasePrefix, null, -1, -1);
	}

	public static <T> SqlRunningBean generateInsertSql(T bean, String databasePrefix, String tablePrefix) {
		return generateInsertSql(bean, databasePrefix, tablePrefix, -1, -1);
	}

	public static <T> SqlRunningBean generateUpdateSql(T bean, String databasePrefix, String tablePrefix, int databaseIndex, int tableIndex) {
		final StringBuilder sb = new StringBuilder();
		sb.append(" update ");

		if (StringUtils.isEmpty(tablePrefix))
			tablePrefix = OrmUtil.javaClassName2DbTableName(bean.getClass().getSimpleName());

		sb.append(getQualifiedTableName(databasePrefix, tablePrefix, databaseIndex, tableIndex));

		sb.append(" set ");

		final List<Object> params = new LinkedList<Object>();

		new FieldVisitor<T>(bean).visit(new FieldHandler() {
			public void handle(int index, Field field, Object value) {
				if (index != 0)
					sb.append(", ");

				sb.append(OrmUtil.javaFieldName2DbFieldName(field.getName())).append("=? ");

				if (value instanceof Enum)
					value = ((Enum<?>) value).ordinal();

				params.add(value);
			}
		});

		sb.append(" where ID = ?");

		params.add(ReflectionUtil.getFieldValue(bean, "id"));

		return new SqlRunningBean(sb.toString(), params.toArray());
	}

	public static <T> SqlRunningBean generateUpdateSql(T bean) {
		return generateUpdateSql(bean, null, null, -1, -1);
	}

	public static <T> SqlRunningBean generateUpdateSql(T bean, String databasePrefix) {
		return generateUpdateSql(bean, databasePrefix, null, -1, -1);
	}

	public static <T> SqlRunningBean generateUpdateSql(T bean, String databasePrefix, String tablePrefix) {
		return generateUpdateSql(bean, databasePrefix, tablePrefix, -1, -1);
	}

	public static <T> SqlRunningBean generateDeleteSql(long id, Class<T> clazz, String databasePrefix, String tablePrefix, int databaseIndex, int tableIndex) {
		final StringBuilder sb = new StringBuilder();
		sb.append("delete from ");

		if (StringUtils.isEmpty(tablePrefix))
			tablePrefix = OrmUtil.javaClassName2DbTableName(clazz.getSimpleName());

		sb.append(getQualifiedTableName(databasePrefix, tablePrefix, databaseIndex, tableIndex));

		sb.append(" where ID = ?");

		List<Object> params = new LinkedList<Object>();
		params.add(id);

		return new SqlRunningBean(sb.toString(), params.toArray());
	}

	public static <T> SqlRunningBean generateDeleteSql(long id, Class<T> clazz) {
		return generateDeleteSql(id, clazz, null, null, -1, -1);
	}

	public static <T> SqlRunningBean generateDeleteSql(long id, Class<T> clazz, String databasePrefix) {
		return generateDeleteSql(id, clazz, databasePrefix, null, -1, -1);
	}

	public static <T> SqlRunningBean generateDeleteSql(long id, Class<T> clazz, String databasePrefix, String tablePrefix) {
		return generateDeleteSql(id, clazz, databasePrefix, tablePrefix, -1, -1);
	}

	public static <T> SqlRunningBean generateSelectSql(String name, Object value, Class<T> clazz, String databasePrefix, String tablePrefix, int databaseIndex, int tableIndex) {
		StringBuilder sb = new StringBuilder();
		sb.append("select * from ");

		if (StringUtils.isEmpty(tablePrefix))
			tablePrefix = OrmUtil.javaClassName2DbTableName(clazz.getSimpleName());

		sb.append(getQualifiedTableName(databasePrefix, tablePrefix, databaseIndex, tableIndex));

		sb.append(" where ");
		sb.append(name).append("=?");

		List<Object> params = new LinkedList<Object>();
		params.add(value);

		return new SqlRunningBean(sb.toString(), params.toArray());
	}

	public static <T> SqlRunningBean generateSelectSql(String name, Object value, Class<T> clazz) {
		return generateSelectSql(name, value, clazz, null, null, -1, -1);
	}

	public static <T> SqlRunningBean generateSelectSql(String name, Object value, Class<T> clazz, String databasePrefix) {
		return generateSelectSql(name, value, clazz, databasePrefix, null, -1, -1);
	}

	public static <T> SqlRunningBean generateSelectSql(String name, Object value, Class<T> clazz, String databasePrefix, String tablePrefix) {
		return generateSelectSql(name, value, clazz, databasePrefix, tablePrefix, -1, -1);
	}

	public static <T> SqlRunningBean generateSearchSql(T bean, String name, Object valueFrom, Object valueTo, String databasePrefix, String tablePrefix, int databaseIndex, int tableIndex) {
		final StringBuilder sb = new StringBuilder();
		sb.append("select * from ");

		if (StringUtils.isEmpty(tablePrefix))
			tablePrefix = OrmUtil.javaClassName2DbTableName(bean.getClass().getSimpleName());
		sb.append(getQualifiedTableName(databasePrefix, tablePrefix, databaseIndex, tableIndex));

		sb.append(" where ");

		final List<Object> params = new LinkedList<Object>();

		new FieldVisitor<T>(bean).visit(new FieldHandler() {
			public void handle(int index, Field field, Object value) {
				if (index != 0)
					sb.append(" and ");

				sb.append(OrmUtil.javaFieldName2DbFieldName(field.getName())).append("=? ");

				if (value instanceof Enum)
					value = ((Enum<?>) value).ordinal();

				params.add(value);
			}
		});

		if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(valueFrom) && !StringUtils.isEmpty(valueTo)) {
			sb.append(" and ").append(name).append(">=? and ");
			sb.append(name).append("<=? ");
			params.add(valueFrom);
			params.add(valueTo);
		} else if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(valueFrom) && StringUtils.isEmpty(valueTo)) {
			sb.append(" and ").append(name).append("=? ");
			params.add(valueFrom);
		}

		return new SqlRunningBean(sb.toString(), params.toArray());
	}

	public static <T> SqlRunningBean generateSearchSql(T bean, String databasePrefix, String tablePrefix, int databaseIndex, int tableIndex) {
		return generateSearchSql(bean, null, null, null, databasePrefix,
				tablePrefix, databaseIndex, tableIndex);
	}

	public static <T> SqlRunningBean generateSearchSql(T bean, String name, Object value, String databasePrefix, String tablePrefix, int databaseIndex, int tableIndex) {
		return generateSearchSql(bean, name, value, null, databasePrefix, tablePrefix, databaseIndex, tableIndex);
	}

	public static <T> SqlRunningBean generateSearchSql(T bean) {
		return generateSearchSql(bean, null, null, null, null, null, -1, -1);
	}

	/**生成带有分片下标的表名
	 * @param databasePrefix
	 * @param tablePrefix
	 * @param dbIndex
	 * @param tableIndex
	 * @return
	 */
	private static String getQualifiedTableName(String databasePrefix, String tablePrefix, int dbIndex, int tableIndex) {
		StringBuffer sb = new StringBuffer();

		if (!StringUtils.isEmpty(databasePrefix))
			sb.append(databasePrefix);

		if (dbIndex != -1)
			sb.append("_").append(dbIndex).append(".");

		if (!StringUtils.isEmpty(tablePrefix))
			sb.append(tablePrefix);

		if (tableIndex != -1)
			sb.append("_").append(tableIndex).append(" ");

		return sb.toString();
	}
}