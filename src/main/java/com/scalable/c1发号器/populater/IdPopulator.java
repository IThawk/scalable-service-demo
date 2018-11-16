package com.scalable.c1发号器.populater;

import com.scalable.c1发号器.bean.Id;
import com.scalable.c1发号器.bean.IdMeta;
import com.scalable.c1发号器.timer.Timer;

/**计算时间和序列号，我们使用synchronized锁、ReentrantLock以及CAS来实现，其中ReentrantLock是默认的实现方式。
 * @author Administrator
 *
 */
public interface IdPopulator {

    void populateId(Timer timer, Id id, IdMeta idMeta);

}