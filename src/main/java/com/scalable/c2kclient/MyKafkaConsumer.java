package com.scalable.c2kclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.scalable.c2kclient.handlers.MessageHandler;

public class MyKafkaConsumer {

	protected static Logger log = LoggerFactory.getLogger(MyKafkaConsumer.class);

	private String propertiesFile;
	private Properties properties;
	private String topic;
	private int streamNum;

	private MessageHandler handler;

	private ExecutorService streamThreadPool;

	private ExecutorService sharedAsyncThreadPool;

	private ConsumerConnector consumerConnector;

	private List<KafkaStream<String, String>> streams;
	private boolean isAutoCommitOffset = true;

	enum Status {
		INIT, RUNNING, STOPPING, STOPPED;
	};

	private volatile Status status = Status.INIT;

	private int fixedThreadNum = 0;

	private int minThreadNum = 0;
	private int maxThreadNum = 0;

	private boolean isAsyncThreadModel = false;
	private boolean isSharedAsyncThreadPool = false;

	private List<AbstractMessageTask> tasks;

	public MyKafkaConsumer() {
		// For Spring context
	}

	/**使用同步线程池的构造器
	 * @param propertiesFile
	 * @param topic
	 * @param streamNum：代表创建的消息流的数量
	 * @param handler
	 */
	public MyKafkaConsumer(String propertiesFile, String topic, int streamNum, MessageHandler handler) {
		this(propertiesFile, topic, streamNum, 0, false, handler);
	}

	public MyKafkaConsumer(String propertiesFile, String topic, int streamNum, int fixedThreadNum, MessageHandler handler) {
		this(propertiesFile, topic, streamNum, fixedThreadNum, false, handler);
	}

	/**异步固定线程池的构造器
	 * @param propertiesFile
	 * @param topic
	 * @param streamNum
	 * @param fixedThreadNum：代表使用的是异步线程池，以及在异步线程中需要创建的线程数
	 * @param isSharedThreadPool
	 * @param handler
	 */
	public MyKafkaConsumer(String propertiesFile, String topic, int streamNum, int fixedThreadNum, boolean isSharedThreadPool, MessageHandler handler) {
		this.propertiesFile = propertiesFile;
		this.topic = topic;
		this.streamNum = streamNum;
		this.fixedThreadNum = fixedThreadNum;
		this.isSharedAsyncThreadPool = isSharedThreadPool;
		this.handler = handler;
		this.isAsyncThreadModel = (fixedThreadNum != 0);

		init();
	}

	/**异步可变线程池的构造器
	 * @param propertiesFile
	 * @param topic
	 * @param streamNum
	 * @param minThreadNum：代表使用的是异步线程池，以及在异步线程池中需要创建的最大、最小线程数
	 * @param maxThreadNum
	 * @param handler
	 */
	public MyKafkaConsumer(String propertiesFile, String topic, int streamNum, int minThreadNum, int maxThreadNum, MessageHandler handler) {
		this(propertiesFile, topic, streamNum, minThreadNum, maxThreadNum, false, handler);
	}

	public MyKafkaConsumer(String propertiesFile, String topic, int streamNum, int minThreadNum, int maxThreadNum, boolean isSharedThreadPool, MessageHandler handler) {
		this.propertiesFile = propertiesFile;
		this.topic = topic;
		this.streamNum = streamNum;
		this.minThreadNum = minThreadNum;
		this.maxThreadNum = maxThreadNum;
		this.isSharedAsyncThreadPool = isSharedThreadPool;
		this.handler = handler;
		this.isAsyncThreadModel = !(minThreadNum == 0 && maxThreadNum == 0);

		init();
	}

	public MyKafkaConsumer(Properties properties, String topic, int streamNum, MessageHandler handler) {
		this(properties, topic, streamNum, 0, false, handler);
	}

	public MyKafkaConsumer(Properties properties, String topic, int streamNum, int fixedThreadNum, MessageHandler handler) {
		this(properties, topic, streamNum, fixedThreadNum, false, handler);
	}

	public MyKafkaConsumer(Properties properties, String topic, int streamNum, int fixedThreadNum, boolean isSharedThreadPool, MessageHandler handler) {
		this.properties = properties;
		this.topic = topic;
		this.streamNum = streamNum;
		this.fixedThreadNum = fixedThreadNum;
		this.isSharedAsyncThreadPool = isSharedThreadPool;
		this.handler = handler;
		this.isAsyncThreadModel = (fixedThreadNum != 0);

		init();
	}

	public MyKafkaConsumer(Properties properties, String topic, int streamNum, int minThreadNum, int maxThreadNum, MessageHandler handler) {
		this(properties, topic, streamNum, minThreadNum, maxThreadNum, false, handler);
	}

	public MyKafkaConsumer(Properties properties, String topic, int streamNum, int minThreadNum, int maxThreadNum, boolean isSharedThreadPool, MessageHandler handler) {
		this.properties = properties;
		this.topic = topic;
		this.streamNum = streamNum;
		this.minThreadNum = minThreadNum;
		this.maxThreadNum = maxThreadNum;
		this.isSharedAsyncThreadPool = isSharedThreadPool;
		this.handler = handler;
		this.isAsyncThreadModel = !(minThreadNum == 0 && maxThreadNum == 0);

		init();
	}

	public void init() {
		if (properties == null && propertiesFile == null) {
			log.error("The properties object or file can't be null.");
			throw new IllegalArgumentException("The properties object or file can't be null.");
		}

		if (StringUtils.isEmpty(topic)) {
			log.error("The topic can't be empty.");
			throw new IllegalArgumentException("The topic can't be empty.");
		}

		if (isAsyncThreadModel == true && fixedThreadNum <= 0 && (minThreadNum <= 0 || maxThreadNum <= 0)) {
			log.error("Either fixedThreadNum or minThreadNum/maxThreadNum is greater than 0.");
			throw new IllegalArgumentException("Either fixedThreadNum or minThreadNum/maxThreadNum is greater than 0.");
		}

		if (isAsyncThreadModel == true && minThreadNum > maxThreadNum) {
			log.error("The minThreadNum should be less than maxThreadNum.");
			throw new IllegalArgumentException("The minThreadNum should be less than maxThreadNum.");
		}

		if (properties == null)
			properties = loadPropertiesfile();

		if (isSharedAsyncThreadPool) {
			sharedAsyncThreadPool = initAsyncThreadPool();
		}

		initGracefullyShutdown();
		initKafka();
	}

	protected Properties loadPropertiesfile() {
		Properties properties = new Properties();
		try {
			properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFile));
		} catch (IOException e) {
			log.error("The consumer properties file is not loaded.", e);
			throw new IllegalArgumentException("The consumer properties file is not loaded.", e);
		}

		return properties;
	}

	private ExecutorService initAsyncThreadPool() {
		ExecutorService syncThreadPool = null;
		if (fixedThreadNum > 0)
			syncThreadPool = Executors.newFixedThreadPool(fixedThreadNum);
		else
			syncThreadPool = new ThreadPoolExecutor(minThreadNum, maxThreadNum, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

		return syncThreadPool;
	}

	/**
	 * 末尾调用initGracefullyShutdown来初始化关机所需的操作，这里通过增加一个虚拟机来关闭钩子事件，也就是说在虚拟机关闭时调用方法shutdownGracefully
	 */
	protected void initGracefullyShutdown() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				shutdownGracefully();
			}
		});
	}

	/**
	 * 在初始化init方法的最后调用了initKafka方法，其中初始化了连接kafka的消费者连接器、流对象以及流线程池。
	 */
	protected void initKafka() {
		if (handler == null) {
			log.error("Exectuor can't be null!");
			throw new RuntimeException("Exectuor can't be null!");
		}

		log.info("Consumer properties:" + properties);
		ConsumerConfig config = new ConsumerConfig(properties);

		isAutoCommitOffset = config.autoCommitEnable();
		log.info("Auto commit: " + isAutoCommitOffset);

		consumerConnector = Consumer.createJavaConsumerConnector(config);

		Map<String, Integer> topics = new HashMap<String, Integer>();
		topics.put(topic, streamNum);
		StringDecoder keyDecoder = new StringDecoder(new VerifiableProperties());
		StringDecoder valueDecoder = new StringDecoder(new VerifiableProperties());
		Map<String, List<KafkaStream<String, String>>> streamsMap = consumerConnector.createMessageStreams(topics, keyDecoder, valueDecoder);

		streams = streamsMap.get(topic);
		log.info("Streams:" + streams);

		if (streams == null || streams.isEmpty()) {
			log.error("Streams are empty.");
			throw new IllegalArgumentException("Streams are empty.");
		}

		streamThreadPool = Executors.newFixedThreadPool(streamNum);
	}

	/**
	 * 启动流线程，首先判断是否是初始化状态，然后将其设置成执行状态，接着将消费者消息的任务实现对象添加到流线程池中执行。
	 * 消费者的任务实现方式有两种：一种是同步消费消息任务，实现类为SequentialMessageTask，适用于快速返回的事务型任务；一种是异步消费消息任务，实现类为ConcurrentMessageTask，适用于耗时任务。
	 */
	public void startup() {
		if (status != Status.INIT) {
			log.error("The client has been started.");
			throw new IllegalStateException("The client has been started.");
		}

		status = Status.RUNNING;

		log.info("Streams num: " + streams.size());
		tasks = new ArrayList<AbstractMessageTask>();
		for (KafkaStream<String, String> stream : streams) {
			AbstractMessageTask abstractMessageTask = (fixedThreadNum == 0 ? new SequentialMessageTask(stream, handler) : new ConcurrentMessageTask(stream, handler, fixedThreadNum));
			tasks.add(abstractMessageTask);
			streamThreadPool.execute(abstractMessageTask);
		}
	}

	/**
	 * 首先设置关闭标志，然后关闭同步线程池或者异步线程池等，接着关闭消费者本身，并将状态设置为已关闭
	 */
	public void shutdownGracefully() {
		status = Status.STOPPING;

		shutdownThreadPool(streamThreadPool, "main-pool");

		if (isSharedAsyncThreadPool)
			shutdownThreadPool(sharedAsyncThreadPool, "shared-async-pool");
		else
			for (AbstractMessageTask task : tasks) {
				task.shutdown();
			}

		if (consumerConnector != null) {
			consumerConnector.shutdown();
		}

		status = Status.STOPPED;
	}

	/**充分考虑线程池的优雅停止，先通过shutdown等待线程池自身结束，然后等待60秒，如果没有成功，再调用shutdownNow将等待iio的任务中断并退出。
	 * @param threadPool
	 * @param alias
	 */
	private void shutdownThreadPool(ExecutorService threadPool, String alias) {
		log.info("Start to shutdown the thead pool: {}", alias);

		threadPool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
				threadPool.shutdownNow(); // Cancel currently executing tasks
				log.warn("Interrupt the worker, which may cause some task inconsistent. Please check the biz logs.");

				// Wait a while for tasks to respond to being cancelled
				if (!threadPool.awaitTermination(60, TimeUnit.SECONDS))
					log.error("Thread pool can't be shutdown even with interrupting worker threads, which may cause some task inconsistent. Please check the biz logs.");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			threadPool.shutdownNow();
			log.error("The current server thread is interrupted when it is trying to stop the worker threads. This may leave an inconcistent state. Please check the biz logs.");

			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}

		log.info("Finally shutdown the thead pool: {}", alias);
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

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public int getStreamNum() {
		return streamNum;
	}

	public void setStreamNum(int streamNum) {
		this.streamNum = streamNum;
	}

	public MessageHandler getHandler() {
		return handler;
	}

	public void setHandler(MessageHandler handler) {
		this.handler = handler;
	}

	public int getFixedThreadNum() {
		return fixedThreadNum;
	}

	public void setFixedThreadNum(int fixedThreadNum) {
		this.fixedThreadNum = fixedThreadNum;
	}

	public int getMinThreadNum() {
		return minThreadNum;
	}

	public void setMinThreadNum(int minThreadNum) {
		this.minThreadNum = minThreadNum;
	}

	public int getMaxThreadNum() {
		return maxThreadNum;
	}

	public void setMaxThreadNum(int maxThreadNum) {
		this.maxThreadNum = maxThreadNum;
	}

	/**从消息队列中接收消息并处理
	 * @author DELL
	 *
	 */
	abstract class AbstractMessageTask implements Runnable {
		protected KafkaStream<String, String> stream;

		protected MessageHandler messageHandler;

		AbstractMessageTask(KafkaStream<String, String> stream, MessageHandler messageHandler) {
			this.stream = stream;
			this.messageHandler = messageHandler;
		}

		@Override
		public void run() {
			ConsumerIterator<String, String> it = stream.iterator();
			//每次循环都要判断服务器的状态，如果服务器状态为仍然处于运行中，也就是说没有被关闭或者请求关闭，则进入下一次循环，就行消费消息和处理消息。
			while (status == Status.RUNNING) {
				boolean hasNext = false;
				try {
					// When it is interrupted if process is killed, it causes some duplicate message processing, because it commits the message in a chunk every 30 seconds
					//通过消费者初始化时创建的消息流对象，判断在消息流中是否有可处理的消息。需要注意的是，如果没有消息，则这个线程会阻塞，在调用线程池的shutdownNow或者进行接收到
					//中断信号时，能使这个io阻塞中断，因此我们需要捕获异常并处理。在中断后，如果发现不是执行状态，就需要结束循环并退出；如果产生了其它异常，则记录异常到日志，并继续
					//下一次循环，对于这个异常日志，要通过报警系统报出来。
					hasNext = it.hasNext();
				} catch (Exception e) {
					// hasNext() method is implemented by scala, so no checked
					// exception is declared, in addtion, hasNext() may throw
					// Interrupted exception when interrupted, so we have to
					// catch Exception here and then decide if it is interrupted
					// exception
					if (e instanceof InterruptedException) {
						log.info("The worker [Thread ID: {}] has been interrupted when retrieving messages from kafka broker. Maybe the consumer is shutting down.", Thread.currentThread().getId());
						log.error("Retrieve Interrupted: ", e);

						if (status != Status.RUNNING) {
							it.clearCurrentChunk();
							shutdown();
							break;
						}
					} else {
						log.error("The worker [Thread ID: {}] encounters an unknown exception when retrieving messages from kafka broker. Now try again.", Thread.currentThread().getId());
						log.error("Retrieve Error: ", e);
						continue;
					}
				}

				if (hasNext) {
					MessageAndMetadata<String, String> item = it.next();
					log.debug("partition[" + item.partition() + "] offset[" + item.offset() + "] message[" + item.message() + "]");

					//如果正确获取一条消息，则代理到子类中进行处理，子类的处理方式分为同步、异步。如果系统没有开启自动提交，框架会手工提交消息队列的offset，让队列的消费者可以消费下一条消息
					handleMessage(item.message());

					// if not auto commit, commit it manually
					if (!isAutoCommitOffset) {
						consumerConnector.commitOffsets();
					}
				}
			}
		}

		protected void shutdown() {
			// Actually it doesn't work in auto commit mode, because kafka v0.8 commits once per 30 seconds, so it is bound to consume duplicate messages.
			stream.clear();
		}

		protected abstract void handleMessage(String message);
	}

	/**类似于tomcat的bio线程池模型，当消息到达后分配一个线程来处理消息，一直把这个消息处理完，再释放线程。
	 * @author DELL
	 *
	 */
	class SequentialMessageTask extends AbstractMessageTask {
		SequentialMessageTask(KafkaStream<String, String> stream, MessageHandler messageHandler) {
			super(stream, messageHandler);
		}

		@Override
		protected void handleMessage(String message) {
			messageHandler.execute(message);
		}
	}

	/**类似于tomcat的nio线程，当消息到达后，由线程池来获取消息，然后流线程将消息转发给worker线程池来处理任务，是个异步的线程池模型。
	 * 这里，异步线程池模型又分为每个流对应一个线程池和多个流共享一个线程池。
	 * @author DELL
	 *
	 */
	class ConcurrentMessageTask extends AbstractMessageTask {
		private ExecutorService asyncThreadPool;

		ConcurrentMessageTask(KafkaStream<String, String> stream, MessageHandler messageHandler, int threadNum) {
			super(stream, messageHandler);

			if (isSharedAsyncThreadPool)
				asyncThreadPool = sharedAsyncThreadPool;
			else {
				asyncThreadPool = initAsyncThreadPool();
			}
		}

		@Override
		protected void handleMessage(final String message) {
			asyncThreadPool.submit(new Runnable() {
				public void run() {
					// if it blows, how to recover
					messageHandler.execute(message);
				}
			});
		}

		protected void shutdown() {
			if (!isSharedAsyncThreadPool)
				shutdownThreadPool(asyncThreadPool, "async-pool-" + Thread.currentThread().getId());
		}
	}

}
