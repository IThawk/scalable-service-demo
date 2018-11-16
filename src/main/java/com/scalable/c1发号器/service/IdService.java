package com.scalable.c1发号器.service;

import java.util.Date;

import com.scalable.c1发号器.bean.Id;

public interface IdService {
	
	/**这是分布式发号器的主要api，用来产生唯一id
	 * @return
	 */
	public long genId();
	
	/**这是产生唯一id的反向操作，可以对一个id内包含的信息进行解读，用人可读的形式来表达
	 * @param id
	 * @return
	 */
	public Id expId(long id);
	
	/**用来伪造某一时间的id
	 * @param time
	 * @param seq
	 * @return
	 */
	public long makeId(long time, long seq);
	
	public long makeId(long time, long seq, long machine);
	
	public long makeId(long genMethod, long time, long seq, long machine);
	
	public long makeId(long type, long genMethod, long time, long seq, long machine);
	
	/**
	 * @param version 版本
	 * @param type 类型
	 * @param genMethod 生成方式
	 * @param time
	 * @param seq
	 * @param machine 机器id
	 * @return
	 */
	public long makeId(long version, long type, long genMethod, long time, long seq, long machine);
	
	/**该方法用于将整形时间翻译成格式化时间
	 * @param time
	 * @return
	 */
	public Date transTime(long time);

}
