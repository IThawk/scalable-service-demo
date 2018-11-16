package com.scalable.c3split.core.sql.parser;

import java.util.Map;

import org.apache.commons.collections.map.LRUMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.alibaba.druid.sql.parser.Lexer;
import com.alibaba.druid.sql.parser.Token;
import com.scalable.c3split.excep.NotSupportedException;

/**使用了阿里巴巴的Druid框架对Sql进行解析，在解析后提取表名等信息，存放在SplitSqlStructure类型的对象中
 * @author DELL
 *
 */
public class SplitSqlParserDefImpl implements SplitSqlParser {

	private static final Logger log = LoggerFactory.getLogger(SplitSqlParserDefImpl.class);
	private static final int CACHE_SIZE = 1000;

	@SuppressWarnings("unchecked")
	private Map<String, SplitSqlStructure> cache = new LRUMap(CACHE_SIZE);

	public SplitSqlParserDefImpl() {
		log.info("Default SplitSqlParserDefImpl is used.");
	}

	public SplitSqlStructure parseSplitSql(String sql) {
		SplitSqlStructure splitSqlStructure = cache.get(sql);

		// Don't use if contains then get, race conditon may happens due to LRU map
		if (splitSqlStructure != null)
			return splitSqlStructure;

		splitSqlStructure = new SplitSqlStructure();

		String dbName = null;
		String tableName = null;
		boolean inProcess = false;

		boolean previous = true;
		boolean sebsequent = false;
		StringBuffer sbPreviousPart = new StringBuffer();
		StringBuffer sbSebsequentPart = new StringBuffer();

		// Need to opertimize for better performance
		Lexer lexer = new Lexer(sql);
		do {
			lexer.nextToken();
			Token tok = lexer.token();
			if (tok == Token.EOF) {
				break;
			}

			if (tok.name != null)
				switch (tok.name) {
				case "SELECT":
					splitSqlStructure.setSqlType(SplitSqlStructure.SqlType.SELECT);
					break;

				case "INSERT":
					splitSqlStructure.setSqlType(SplitSqlStructure.SqlType.INSERT);
					break;

				case "UPDATE":
					inProcess = true;
					splitSqlStructure.setSqlType(SplitSqlStructure.SqlType.UPDATE);
					break;

				case "DELETE":
					splitSqlStructure.setSqlType(SplitSqlStructure.SqlType.DELETE);
					break;

				case "INTO":
					if (SplitSqlStructure.SqlType.INSERT.equals(splitSqlStructure.getSqlType()))
						inProcess = true;
					break;

				case "FROM":
					if (SplitSqlStructure.SqlType.SELECT.equals(splitSqlStructure.getSqlType())
							|| SplitSqlStructure.SqlType.DELETE.equals(splitSqlStructure.getSqlType()))
						inProcess = true;
					break;
				}

			if (sebsequent)
				sbSebsequentPart.append(tok == Token.IDENTIFIER ? lexer.stringVal() : tok.name).append(" ");

			if (inProcess) {
				if (dbName == null && tok == Token.IDENTIFIER) {
					dbName = lexer.stringVal();
					previous = false;
				} else if (dbName != null && tableName == null && tok == Token.IDENTIFIER) {
					tableName = lexer.stringVal();

					inProcess = false;
					sebsequent = true;
				}
			}

			if (previous)
				sbPreviousPart.append(tok == Token.IDENTIFIER ? lexer.stringVal() : tok.name).append(" ");
		} while (true);

		if (StringUtils.isEmpty(dbName) || StringUtils.isEmpty(tableName))
			throw new NotSupportedException("The split sql is not supported: " + sql);

		splitSqlStructure.setDbName(dbName);
		splitSqlStructure.setTableName(tableName);

		splitSqlStructure.setPreviousPart(sbPreviousPart.toString());
		splitSqlStructure.setSebsequentPart(sbSebsequentPart.toString());

		// if race condition, it is not severe
		if (!cache.containsKey(splitSqlStructure))
			cache.put(sql, splitSqlStructure);

		return splitSqlStructure;
	}
}