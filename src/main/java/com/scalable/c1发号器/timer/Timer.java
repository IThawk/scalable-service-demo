package com.scalable.c1发号器.timer;

import java.util.Date;

import com.scalable.c1发号器.bean.IdMeta;
import com.scalable.c1发号器.bean.IdType;

public interface Timer {
	
	long EPOCH = 1514736000000L;

    void init(IdMeta idMeta, IdType idType);

    Date transTime(long time);

    /**校验服务器时间是否被调慢，如果机器时间被回调了，就会产生重复的id
     * @param lastTimestamp
     * @param timestamp
     */
    void validateTimestamp(long lastTimestamp, long timestamp);

    long tillNextTimeUnit(long lastTimestamp);

    /**返回当前时间，但一般不是直接返回当前时间的毫秒/秒，不然数据会很大，实际上是返回了当前时间和EPOCH的差值
     * @return
     */
    long genTime();

}
