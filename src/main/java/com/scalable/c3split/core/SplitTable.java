package com.scalable.c3split.core;

import java.util.List;

/**主要包括数据库名的前缀、表名的前缀、数据库和表的分片数量等，以及切分策略和读写分离等
 * @author Administrator
 *
 */
public class SplitTable {
	private String dbNamePrefix;
	private String tableNamePrefix;

	private int dbNum;
	private int tableNum;

	private SplitStrategy splitStrategy;
	private List<SplitDb> splitDbs;

	private boolean readWriteSeparate = true;

	public void init() {
		this.splitStrategy = new HorizontalHashSplitStrategy(dbNum, tableNum);
	}

	public String getDbNamePrefix() {
		return dbNamePrefix;
	}

	public void setDbNamePrefix(String dbNamePrifix) {
		this.dbNamePrefix = dbNamePrifix;
	}

	public String getTableNamePrefix() {
		return tableNamePrefix;
	}

	public void setTableNamePrefix(String tableNamePrifix) {
		this.tableNamePrefix = tableNamePrifix;
	}

	public int getDbNum() {
		return dbNum;
	}

	public void setDbNum(int dbNum) {
		this.dbNum = dbNum;
	}

	public int getTableNum() {
		return tableNum;
	}

	public void setTableNum(int tableNum) {
		this.tableNum = tableNum;
	}

	public List<SplitDb> getSplitDbs() {
		return splitDbs;
	}

	public void setSplitDbs(List<SplitDb> splitDbs) {
		this.splitDbs = splitDbs;
	}

	public SplitStrategy getSplitStrategy() {
		return splitStrategy;
	}

	public void setSplitStrategy(SplitStrategy splitStrategy) {
		this.splitStrategy = splitStrategy;
	}

	public boolean isReadWriteSeparate() {
		return readWriteSeparate;
	}

	public void setReadWriteSeparate(boolean readWriteSeparate) {
		this.readWriteSeparate = readWriteSeparate;
	}

}