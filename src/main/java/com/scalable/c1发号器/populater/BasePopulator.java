package com.scalable.c1发号器.populater;

import com.scalable.c1发号器.bean.Id;
import com.scalable.c1发号器.bean.IdMeta;
import com.scalable.c1发号器.timer.Timer;

public abstract class BasePopulator implements IdPopulator, ResetPopulator {
    protected long sequence = 0;
    protected long lastTimestamp = -1;

    public BasePopulator() {
        super();
    }

    //查看当前时间是否已经到了下一个时间单位，如果已经到了下一个时间单位，则将序列号清零；如果还在上一个时间单位，就对序列号累加，如果累加后越界，就需要等待下一秒再产生唯一id
    public void populateId(Timer timer, Id id, IdMeta idMeta) {
        long timestamp = timer.genTime();
        timer.validateTimestamp(lastTimestamp, timestamp);

        if (timestamp == lastTimestamp) {
            sequence++;
            sequence &= idMeta.getSeqBitsMask();
            if (sequence == 0) {
                timestamp = timer.tillNextTimeUnit(lastTimestamp);
            }
        } else {
            lastTimestamp = timestamp;
            sequence = 0;
        }

        id.setSeq(sequence);
        id.setTime(timestamp);
    }

    public void reset() {
        this.sequence = 0;
        this.lastTimestamp = -1;
    }
}