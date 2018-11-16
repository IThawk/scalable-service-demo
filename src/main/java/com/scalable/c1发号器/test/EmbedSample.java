package com.scalable.c1发号器.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.scalable.c1发号器.bean.Id;
import com.scalable.c1发号器.service.IdService;

public class EmbedSample {

	public static void main(String[] args) {
        ApplicationContext ac = new ClassPathXmlApplicationContext("spring/vesta-service-sample.xml");

        IdService idService = (IdService) ac.getBean("idService");
        long id = idService.genId();
        Id ido = idService.expId(id);
        System.out.println(id + ":" + ido);

        IdService idServiceIp = (IdService) ac.getBean("idServiceIp");
        long idIp = idServiceIp.genId();
        Id idoIp = idServiceIp.expId(idIp);

        System.out.println(idIp + ":" + idoIp);

        IdService idServiceDb = (IdService) ac.getBean("idServiceDb");
        long idDb = idServiceDb.genId();
        Id idoDb = idServiceDb.expId(idDb);

        System.out.println(idDb + ":" + idoDb);
    }

}
