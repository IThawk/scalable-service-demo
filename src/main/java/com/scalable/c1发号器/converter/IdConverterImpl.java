package com.scalable.c1发号器.converter;

import com.scalable.c1发号器.bean.Id;
import com.scalable.c1发号器.bean.IdMeta;

/**在主流程的id元数据对象中设置了id的各个属性后，可通过转换器类将id的元数据对象转换成长整型的id，或者将长整型转换为id元数据对象。
 * @author Administrator
 *
 */
public class IdConverterImpl implements IdConverter {

    public IdConverterImpl() {
    }

    public long convert(Id id, IdMeta idMeta) {
        return doConvert(id, idMeta);
    }

    protected long doConvert(Id id, IdMeta idMeta) {
        long ret = 0;

        ret |= id.getMachine();

        ret |= id.getSeq() << idMeta.getSeqBitsStartPos();

        ret |= id.getTime() << idMeta.getTimeBitsStartPos();

        ret |= id.getGenMethod() << idMeta.getGenMethodBitsStartPos();

        ret |= id.getType() << idMeta.getTypeBitsStartPos();

        ret |= id.getVersion() << idMeta.getVersionBitsStartPos();

        return ret;
    }

    public Id convert(long id, IdMeta idMeta) {
        return doConvert(id, idMeta);
    }

    protected Id doConvert(long id, IdMeta idMeta) {
        Id ret = new Id();

        ret.setMachine(id & idMeta.getMachineBitsMask());

        ret.setSeq((id >>> idMeta.getSeqBitsStartPos()) & idMeta.getSeqBitsMask());

        ret.setTime((id >>> idMeta.getTimeBitsStartPos()) & idMeta.getTimeBitsMask());

        ret.setGenMethod((id >>> idMeta.getGenMethodBitsStartPos()) & idMeta.getGenMethodBitsMask());

        ret.setType((id >>> idMeta.getTypeBitsStartPos()) & idMeta.getTypeBitsMask());

        ret.setVersion((id >>> idMeta.getVersionBitsStartPos()) & idMeta.getVersionBitsMask());

        return ret;
    }

}
