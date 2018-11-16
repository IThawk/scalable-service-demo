package com.scalable.c1发号器.populater;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.scalable.c1发号器.bean.Id;
import com.scalable.c1发号器.bean.IdMeta;
import com.scalable.c1发号器.timer.Timer;

public class LockIdPopulator extends BasePopulator {

    private Lock lock = new ReentrantLock();

    public LockIdPopulator() {
        super();
    }

    public void populateId(Timer timer, Id id, IdMeta idMeta) {
        lock.lock();
        try {
            super.populateId(timer, id, idMeta);
        } finally {
            lock.unlock();
        }
    }

}
