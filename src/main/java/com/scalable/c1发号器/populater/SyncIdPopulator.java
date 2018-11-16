package com.scalable.c1发号器.populater;

import com.scalable.c1发号器.bean.Id;
import com.scalable.c1发号器.bean.IdMeta;
import com.scalable.c1发号器.timer.Timer;

public class SyncIdPopulator extends BasePopulator {

    public SyncIdPopulator() {
        super();
    }

    public synchronized void populateId(Timer timer, Id id, IdMeta idMeta) {
        super.populateId(timer, id, idMeta);
    }

}