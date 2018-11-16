package com.scalable.c2kclient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scalable.c2kclient.boot.KClientBoot;
import com.scalable.c2kclient.boot.KafkaHandler;
import com.scalable.c2kclient.domain.Dog;

@RestController
public class KClientController {
	protected static Logger log = LoggerFactory.getLogger(KClientApplication.class);

	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private AnimalsHandler animalsHandler;
	@Autowired
	private KClientBoot kClientBoot;
	
	/**发消息
	 * @return
	 */
	@RequestMapping("/send")
	public String send() {
		List<KafkaHandler> kafkaHandlers = kClientBoot.getKafkaHandlers();
		for (KafkaHandler kafkaHandler : kafkaHandlers) {
			if(kafkaHandler.getKafkaProducer() != null){
				MyKafkaProducer producer = kafkaHandler.getKafkaProducer();
				producer.sendBean2Topic("test", new Dog(1l, "测试"));
			}
		}
		return "ok";
	}

	/**显示欢迎词
	 * @return
	 */
	@RequestMapping("/")
	public String hello() {
		return "Greetings from kclient processor!";
	}

	/**服务的状态，用来显示处理器的数量
	 * @return
	 */
	@RequestMapping("/status")
	public String status() {
		return "Handler Number: [" + kClientBoot.getKafkaHandlers().size() + "]";
	}

	/**停止服务
	 * @return
	 */
	@RequestMapping("/stop")
	public String stop() {
		log.info("Shutdowning KClient now...");
		kClientBoot.shutdownAll();

		String startupTime = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z").format(new Date(applicationContext.getStartupDate()));
		log.info("KClient application stops at: " + startupTime);

		return "KClient application stops at: " + startupTime;
	}

	/**重启服务
	 * @return
	 */
	@RequestMapping("/restart")
	public String restart() {
		log.info("Shutdowning KClient now...");
		kClientBoot.shutdownAll();

		log.info("Restarting KClient now...");

		String startupTime = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z").format(new Date(applicationContext.getStartupDate()));
		log.info("KClient application restarts at: " + startupTime);

		return "KClient application restarts at: " + startupTime;
	}
	
}