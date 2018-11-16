package com.scalable.c1发号器.converter;

import com.scalable.c1发号器.bean.Id;
import com.scalable.c1发号器.bean.IdMeta;

public interface IdConverter {
	
	long convert(Id id, IdMeta idMeta);

    Id convert(long id, IdMeta idMeta);

}
