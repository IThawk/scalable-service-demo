package com.scalable.c2kclient.boot;

import java.util.List;

import com.scalable.c2kclient.MyKafkaConsumer;
import com.scalable.c2kclient.MyKafkaProducer;
import com.scalable.c2kclient.excephandler.ExceptionHandler;

public class KafkaHandler {
	private MyKafkaConsumer kafkaConsumer;

	private MyKafkaProducer kafkaProducer;

	private List<ExceptionHandler> excepHandlers;

	private KafkaHandlerMeta kafkaHandlerMeta;

	public KafkaHandler(MyKafkaConsumer kafkaConsumer, MyKafkaProducer kafkaProducer, List<ExceptionHandler> excepHandlers, KafkaHandlerMeta kafkaHandlerMeta) {
		super();
		this.kafkaConsumer = kafkaConsumer;
		this.kafkaProducer = kafkaProducer;
		this.excepHandlers = excepHandlers;
		this.kafkaHandlerMeta = kafkaHandlerMeta;
	}

	public MyKafkaConsumer getKafkaConsumer() {
		return kafkaConsumer;
	}

	public void setKafkaConsumer(MyKafkaConsumer kafkaConsumer) {
		this.kafkaConsumer = kafkaConsumer;
	}

	public MyKafkaProducer getKafkaProducer() {
		return kafkaProducer;
	}

	public void setKafkaProducer(MyKafkaProducer kafkaProducer) {
		this.kafkaProducer = kafkaProducer;
	}

	public List<ExceptionHandler> getExcepHandlers() {
		return excepHandlers;
	}

	public void setExcepHandlers(List<ExceptionHandler> excepHandlers) {
		this.excepHandlers = excepHandlers;
	}

	public KafkaHandlerMeta getKafkaHandlerMeta() {
		return kafkaHandlerMeta;
	}

	public void setKafkaHandlerMeta(KafkaHandlerMeta kafkaHandlerMeta) {
		this.kafkaHandlerMeta = kafkaHandlerMeta;
	}

}