package com.scalable.c4cache;

/**也是由于缓存行影响性能的示例：
 * 创建了1024*1024*8个长整型数组，首先顺序访问每个长整型数组，按照对缓存行的分析，每8个长整型数组占用一个缓存行，也就是说，我们存取
 * 8个长整型数组才需要去l3缓存交换一次数据，这大大提高了缓存的使用效率。然后我们换一种方式进行测试，每次跳跃性访问数组，每次以一行为步长进行
 * 跳跃，期待每次访问一个元素时，操作系统都从l3缓存取数据。
 * 
 * 通过结果分析发现，顺序访问的速度每次都快于跳跃访问的速度，这验证了我们对缓存行的理论分析。
 * @author Administrator
 *
 */
public class CacheLineDemo {

	//缓存行的大小为64个字节，即8个长整型
	private final static int CACHE_LINE_LONG_NUM = 8;
	//用于测试的缓存行的数量
	private final static int LINE_NUM = 1024 * 1024;
	//一次测试的次数
	private final static int NUM_TEST_TIMES = 10;
	//构造能够填充LINE_NUM个缓存行的数组
	private static final long[] values = new long[CACHE_LINE_LONG_NUM * LINE_NUM];
	
	public static long runOneTestWithAlign(){
		final long start = System.nanoTime();
		//进行顺序读取测试，期待在存取每个缓存行的第i个长整型变量时，系统自动缓存整个缓存行，本行的后续存取都会命中缓存
		for(int i=0; i<CACHE_LINE_LONG_NUM * LINE_NUM; i++){
			values[i] = i;
		}
		return System.nanoTime() - start;
	}
	
	public static long runOneTestWithoutAlign(){
		final long start = System.nanoTime();
		//按照缓存行的步长进行跳跃读取测试，期待每次读取一行中的一个元素，每次读取都不会命中缓存
		for(int i=0; i<CACHE_LINE_LONG_NUM; i++){
			for(int j=0; j<LINE_NUM; j++){
				values[j * CACHE_LINE_LONG_NUM + i] = i * j;
			}
		}
		return System.nanoTime() - start;
	}
	
	public static boolean runOneCompare(){
		long t1 = runOneTestWithAlign();
		long t2 = runOneTestWithoutAlign();
		System.out.println("Sequential: " + t1);
		System.out.println("    Leap: " + t2);
		return t1 < t2;
	}
	
	public static void runOneSuit(int testNum) throws Exception{
		int expectedCount = 0;
		for(int i=0; i<testNum; i++){
			if(runOneCompare()){
				expectedCount++;
			}
		}
		//计算在顺序访问数组的测试场景下，响应时间更短的情况的概率
		System.out.println("Radio (Sequential < leap): " + expectedCount * 100D/ testNum + "%");
	}
	
	public static void main(String[] args) throws Exception {
		runOneSuit(NUM_TEST_TIMES);
	}
	
}
