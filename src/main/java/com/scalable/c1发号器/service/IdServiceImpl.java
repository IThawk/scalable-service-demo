package com.scalable.c1发号器.service;

import com.scalable.c1发号器.bean.Id;
import com.scalable.c1发号器.bean.IdType;
import com.scalable.c1发号器.populater.AtomicIdPopulator;
import com.scalable.c1发号器.populater.IdPopulator;
import com.scalable.c1发号器.populater.LockIdPopulator;
import com.scalable.c1发号器.populater.SyncIdPopulator;
import com.scalable.c1发号器.util.CommonUtils;

/**通过代理模式代理到某个IdPopulator接口的一个实现来计算时间字段和序列号字段
 * @author Administrator
 *
 */
public class IdServiceImpl extends AbstractIdServiceImpl {

    private static final String SYNC_LOCK_IMPL_KEY = "vesta.sync.lock.impl.key";

    private static final String ATOMIC_IMPL_KEY = "vesta.atomic.impl.key";

    protected IdPopulator idPopulator;

    public IdServiceImpl() {
        super();
    }

    public IdServiceImpl(String type) {
        super(type);
    }

    public IdServiceImpl(long type) {
        super(type);
    }

    public IdServiceImpl(IdType type) {
        super(type);
    }

    @Override
    public void init() {
        super.init();
        initPopulator();
    }

    public void initPopulator() {
        if (idPopulator != null){
            log.info("The " + idPopulator.getClass().getCanonicalName() + " is used.");
        } else if (CommonUtils.isPropKeyOn(SYNC_LOCK_IMPL_KEY)) {
            log.info("The SyncIdPopulator is used.");
            idPopulator = new SyncIdPopulator();
        } else if (CommonUtils.isPropKeyOn(ATOMIC_IMPL_KEY)) {
            log.info("The AtomicIdPopulator is used.");
            idPopulator = new AtomicIdPopulator();
        } else {
            log.info("The default LockIdPopulator is used.");
            idPopulator = new LockIdPopulator();
        }
    }

    protected void populateId(Id id) {
        idPopulator.populateId(timer, id, idMeta);
    }

    public void setIdPopulator(IdPopulator idPopulator) {
        this.idPopulator = idPopulator;
    }
}