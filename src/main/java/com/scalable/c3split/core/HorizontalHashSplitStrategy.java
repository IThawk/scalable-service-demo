package com.scalable.c3split.core;

/**水平下标策略：
 * inst0
 * 	db0
 * 		table0
 * 		table1
 * 		table2
 * 		table3
 * 	db1
 * 		table4
 * 		table5
 * 		table6
 * 		table7
 * inst1
 * 	db2
 * 		table8
 * 		table9
 * 		table10
 * 		table11
 * 	db3
 * 		table12
 * 		table13
 * 		table14
 * 		table15
 * @author DELL
 *
 */
public class HorizontalHashSplitStrategy implements SplitStrategy {
	private int dbNum;
	private int tableNum;

	public HorizontalHashSplitStrategy() {

	}

	public HorizontalHashSplitStrategy(int dbNum, int tableNum) {
		this.dbNum = dbNum;
		this.tableNum = tableNum;
	}

	public int getDbNo(Object splitKey) {
		return getTableNo(splitKey) / tableNum;
	}

	public int getTableNo(Object splitKey) {
		int hashCode = calcHashCode(splitKey);
		return hashCode % (dbNum * tableNum);
	}

	private int calcHashCode(Object splitKey) {
		int hashCode = splitKey.hashCode();
		if (hashCode < 0)
			hashCode = -hashCode;

		return hashCode;
	}
}