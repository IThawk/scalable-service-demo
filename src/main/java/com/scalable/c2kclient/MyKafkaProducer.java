package com.scalable.c2kclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cdel.util.helper.JacksonUtil;

/**生产者是对消息发送逻辑进行封装的类，通过指定某个队列的名称，就可以直接将一个领域对象模型或者消息发送出去
 * @author Administrator
 *
 */
public class MyKafkaProducer {

	protected static Logger log = LoggerFactory.getLogger(KafkaProducer.class);

	// If the number of one batch is over 20, use 20 instead
	protected static int MULTI_MSG_ONCE_SEND_NUM = 20;

	private Producer<String, String> producer;

	private String defaultTopic;

	private String propertiesFile;
	private Properties properties;

	public MyKafkaProducer() {
		// For Spring context
	}

	public MyKafkaProducer(String propertiesFile, String defaultTopic) {
		this.propertiesFile = propertiesFile;
		this.defaultTopic = defaultTopic;

		init();
	}

	public MyKafkaProducer(Properties properties, String defaultTopic) {
		this.properties = properties;
		this.defaultTopic = defaultTopic;

		init();
	}

	protected void init() {
		if (properties == null) {
			properties = new Properties();
			try {
				properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFile));
			} catch (IOException e) {
				log.error("The properties file is not loaded.", e);
				throw new IllegalArgumentException("The properties file is not loaded.", e);
			}
		}
		log.info("Producer properties:" + properties);
		producer = new KafkaProducer<>(properties);
	}

	// send string message
	public void send(String message) {
		send2Topic(null, message);
	}

	public void send2Topic(String topicName, String message) {
		if (message == null) {
			return;
		}

		if (topicName == null)
			topicName = defaultTopic;

		ProducerRecord<String, String> km = new ProducerRecord<String, String>(topicName, message);
		producer.send(km);
	}

	public void send(String key, String message) {
		send2Topic(null, key, message);
	}

	public void send2Topic(String topicName, String key, String message) {
		if (message == null) {
			return;
		}

		if (topicName == null)
			topicName = defaultTopic;

		ProducerRecord<String, String> km = new ProducerRecord<String, String>(topicName, key, message);
		producer.send(km);
	}

	public void send(Collection<String> messages) {
		send2Topic(null, messages);
	}

	public void send2Topic(String topicName, Collection<String> messages) {
		if (messages == null || messages.isEmpty()) {
			return;
		}

		if (topicName == null)
			topicName = defaultTopic;

		for (String entry : messages) {
			ProducerRecord<String, String> km = new ProducerRecord<String, String>(topicName, entry);
			producer.send(km);
		}
	}

	public void send(Map<String, String> messages) {
		send2Topic(null, messages);
	}

	public void send2Topic(String topicName, Map<String, String> messages) {
		if (messages == null || messages.isEmpty()) {
			return;
		}

		if (topicName == null)
			topicName = defaultTopic;

		for (Entry<String, String> entry : messages.entrySet()) {
			ProducerRecord<String, String> km = new ProducerRecord<String, String>(topicName, entry.getKey(), entry.getValue());
			producer.send(km);
		}
	}

	// send bean message
	public <T> void sendBean(T bean) {
		sendBean2Topic(null, bean);
	}

	public <T> void sendBean2Topic(String topicName, T bean) {
		send2Topic(topicName, JacksonUtil.ObjecttoJSon(bean));
	}

	public <T> void sendBean(String key, T bean) {
		sendBean2Topic(null, key, bean);
	}

	public <T> void sendBean2Topic(String topicName, String key, T bean) {
		send2Topic(topicName, key, JacksonUtil.ObjecttoJSon(bean));
	}

	public <T> void sendBeans(Collection<T> beans) {
		sendBeans2Topic(null, beans);
	}

	public <T> void sendBeans2Topic(String topicName, Collection<T> beans) {
		Collection<String> beanStrs = new ArrayList<String>();
		for (T bean : beans) {
			beanStrs.add(JacksonUtil.ObjecttoJSon(bean));
		}

		send2Topic(topicName, beanStrs);
	}

	public <T> void sendBeans(Map<String, T> beans) {
		sendBeans2Topic(null, beans);
	}

	public <T> void sendBeans2Topic(String topicName, Map<String, T> beans) {
		Map<String, String> beansStr = new HashMap<String, String>();
		for (Map.Entry<String, T> entry : beans.entrySet()) {
			beansStr.put(entry.getKey(), JacksonUtil.ObjecttoJSon(entry.getValue()));
		}

		send2Topic(topicName, beansStr);
	}

	public void close() {
		producer.close();
	}

	public String getDefaultTopic() {
		return defaultTopic;
	}

	public void setDefaultTopic(String defaultTopic) {
		this.defaultTopic = defaultTopic;
	}

	public String getPropertiesFile() {
		return propertiesFile;
	}

	public void setPropertiesFile(String propertiesFile) {
		this.propertiesFile = propertiesFile;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
}
