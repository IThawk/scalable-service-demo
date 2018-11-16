package com.scalable.c2kclient;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.scalable.c2kclient.boot.ErrorHandler;
import com.scalable.c2kclient.boot.InputConsumer;
import com.scalable.c2kclient.boot.KafkaHandlers;
import com.scalable.c2kclient.boot.OutputProducer;
import com.scalable.c2kclient.domain.Cat;
import com.scalable.c2kclient.domain.Dog;

@Component
@KafkaHandlers
public class AnimalsHandler {
	
	@InputConsumer(propertiesFile = "kafka-consumer.properties", topic = "test", streamNum = 1)
	@OutputProducer(propertiesFile = "kafka-producer.properties", defaultTopic = "test1")
	public Cat dogHandler(Dog dog) {
		System.out.println("Annotated dogHandler handles: " + dog);
		return new Cat(dog);
	}

	@InputConsumer(propertiesFile = "kafka-consumer.properties", topic = "test1", streamNum = 1)
	public void catHandler(Cat cat) throws IOException {
		System.out.println("Annotated catHandler handles: " + cat);
		//throw new IOException("Man made exception.");
	}

	@ErrorHandler(exception = IOException.class, topic = "test1")
	public void ioExceptionHandler(IOException e, String message) {
		System.out.println("Annotated excepHandler handles: " + e);
	}
	
}