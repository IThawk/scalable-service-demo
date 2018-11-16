package com.scalable.c3split.core.sql.parser;

public interface SplitSqlParser {
	public static final SplitSqlParser INST = new SplitSqlParserDefImpl();

	/**解析sql
	 * @param sql
	 * @return
	 */
	public SplitSqlStructure parseSplitSql(String sql);
}