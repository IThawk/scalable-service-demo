package com.scalable.c1发号器.populater;

import java.util.concurrent.atomic.AtomicReference;
import com.scalable.c1发号器.bean.Id;
import com.scalable.c1发号器.bean.IdMeta;
import com.scalable.c1发号器.timer.Timer;

/**CAS修改过程：取得并保存原来的变量，这个变量包含原来的时间和序列号字段；
 * 基于原来的变量计算新的时间和序列号字段，计算逻辑和Synchronized一致；
 * 计算后，使用cas操作更新原来的变量，在更新的过程中，需要传递保存的原来的变量；
 * 如果保存的原来的变量被其它线程改变了，就需要在这里重新拿到最新的变量，并再次计算和尝试更新。
 * @author Administrator
 *
 */
public class AtomicIdPopulator implements IdPopulator, ResetPopulator {

    class Variant {

        private long sequence = 0;
        private long lastTimestamp = -1;

    }

    private AtomicReference<Variant> variant = new AtomicReference<Variant>(new Variant());

    public AtomicIdPopulator() {
        super();
    }

    public void populateId(Timer timer, Id id, IdMeta idMeta) {
        Variant varOld, varNew;
        long timestamp, sequence;

        while (true) {

            // Save the old variant
            varOld = variant.get();

            // populate the current variant
            timestamp = timer.genTime();
            timer.validateTimestamp(varOld.lastTimestamp, timestamp);

            sequence = varOld.sequence;

            if (timestamp == varOld.lastTimestamp) {
                sequence++;
                sequence &= idMeta.getSeqBitsMask();
                if (sequence == 0) {
                    timestamp = timer.tillNextTimeUnit(varOld.lastTimestamp);
                }
            } else {
                sequence = 0;
            }

            // Assign the current variant by the atomic tools
            varNew = new Variant();
            varNew.sequence = sequence;
            varNew.lastTimestamp = timestamp;

            if (variant.compareAndSet(varOld, varNew)) {
                id.setSeq(sequence);
                id.setTime(timestamp);

                break;
            }

        }
    }

    public void reset() {
        variant = new AtomicReference<Variant>(new Variant());
    }

}