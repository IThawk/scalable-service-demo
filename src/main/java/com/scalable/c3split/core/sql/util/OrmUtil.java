package com.scalable.c3split.core.sql.util;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.ResultSetMetaData;
import com.scalable.c3split.util.reflect.ReflectionUtil;

public abstract class OrmUtil {
	private static final Logger log = LoggerFactory.getLogger(OrmUtil.class);

	/**把java的类名转换为数据库的名称，java一般是驼峰的，数据库一般是下划线的
	 * @param name
	 * @return
	 */
	public static String javaClassName2DbTableName(String name) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < name.length(); i++) {
			if (Character.isUpperCase(name.charAt(i)) && i != 0) {
				sb.append("_");
			}

			sb.append(Character.toLowerCase(name.charAt(i)));

		}
		return sb.toString();
	}

	/**将java属性名的驼峰命名方式改为数据库的下划线方式(暂时不用转换)
	 * @param name
	 * @return
	 */
	public static String javaFieldName2DbFieldName(String name) {
		/**
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < name.length(); i++) {
			if (Character.isUpperCase(name.charAt(i))) {
				sb.append("_");
			}

			sb.append(Character.toUpperCase(name.charAt(i)));

		}
		return sb.toString();
		*/
		return name;
	}

	public static String dbFieldName2JavaFieldName(String name) {
		/**
		StringBuilder sb = new StringBuilder();

		boolean lower = true;
		for (int i = 0; i < name.length(); i++) {
			if (name.charAt(i) == '_') {
				lower = false;
				continue;
			}

			if (lower)
				sb.append(Character.toLowerCase(name.charAt(i)));
			else {
				sb.append(Character.toUpperCase(name.charAt(i)));
				lower = true;
			}

		}
		return sb.toString();
		*/
		return name;
	}

	public static String generateParamPlaceholders(int count) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < count; i++) {
			if (i != 0)
				sb.append(",");
			sb.append("?");
		}

		return sb.toString();
	}

	/**把ResultSet中某一行的数据转换为领域对象模型
	 * @param rs
	 * @param clazz
	 * @return
	 */
	public static <T> T convertRow2Bean(ResultSet rs, Class<T> clazz) {
		try {
			T bean = clazz.newInstance();

			ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				int columnType = rsmd.getColumnType(i);
				String columnName = rsmd.getColumnName(i);
				//String fieldName = dbFieldName2JavaFieldName(columnName);
				String fieldName = columnName; //数据库的字段名不用下划线
				String setterName = ReflectionUtil.fieldName2SetterName(fieldName);

				if (columnType == Types.SMALLINT) {
					Method setter = ReflectionUtil.searchEnumSetter(clazz, setterName);
					Class<?> enumParamClazz = setter.getParameterTypes()[0];
					Method enumParseFactoryMethod = enumParamClazz.getMethod("parse", int.class);
					Object value = enumParseFactoryMethod.invoke(enumParamClazz, rs.getInt(i));
					setter.invoke(bean, value);
				} else {
					Class<? extends Object> param = null;
					Object value = null;
					switch (columnType) {
					case Types.VARCHAR:
						param = String.class;
						value = rs.getString(i);
						break;
					case Types.BIGINT:
						param = Long.class;
						value = rs.getLong(i);
						break;
					case Types.INTEGER:
						param = Integer.class;
						value = rs.getInt(i);
						break;
					case Types.DATE:
						param = Date.class;
						value = rs.getTimestamp(i);
						break;
					case Types.TIMESTAMP:
						param = Date.class;
						value = rs.getTimestamp(i);
						break;
					default:
						log.error("Dbsplit doesn't support column {} type {}.", columnName, columnType);
						throw new Exception("Db column not supported.");
					}

					Method setter = clazz.getMethod(setterName, param);
					setter.invoke(bean, value);
				}
			}

			return bean;
		} catch (Exception e) {
			log.error("Fail to operator on ResultSet metadata for clazz {}.", clazz);
			log.error("Exception--->", e);
			throw new IllegalStateException("Fail to operator on ResultSet metadata.", e);
		}
	}
}