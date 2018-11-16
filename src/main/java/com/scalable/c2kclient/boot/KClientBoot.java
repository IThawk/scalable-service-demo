package com.scalable.c2kclient.boot;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.cdel.util.helper.JacksonUtil;
import com.scalable.c2kclient.MyKafkaConsumer;
import com.scalable.c2kclient.MyKafkaProducer;
import com.scalable.c2kclient.excephandler.ExceptionHandler;
import com.scalable.c2kclient.handlers.BeanMessageHandler;
import com.scalable.c2kclient.handlers.BeansMessageHandler;
import com.scalable.c2kclient.handlers.MessageHandler;
import com.scalable.c2kclient.util.AnnotationHandler;
import com.scalable.c2kclient.util.AnnotationTranversor;
import com.scalable.c2kclient.util.TranversorContext;

@Component
public class KClientBoot implements ApplicationContextAware {
	protected static Logger log = LoggerFactory.getLogger(KClientBoot.class);

	private ApplicationContext applicationContext;

	private List<KafkaHandlerMeta> meta = new ArrayList<KafkaHandlerMeta>();

	private List<KafkaHandler> kafkaHandlers = new ArrayList<KafkaHandler>();

	public KClientBoot() {
		// For Spring Context
	}

	/**
	 * 会读取spring环境中的所有bean，如果bean标注有Kafka？Handlers，则对其中的方法进行解析，读取方法上的InputConsumer等注解，然后构造相应的生产者和消费者的对象
	 */
	@PostConstruct
	public void init() {
		meta = getKafkaHandlerMeta();

		if (meta.size() == 0)
			throw new IllegalArgumentException("No handler method is declared in this spring context.");

		for (final KafkaHandlerMeta kafkaHandlerMeta : meta) {
			createKafkaHandler(kafkaHandlerMeta);
		}
	}

	protected List<KafkaHandlerMeta> getKafkaHandlerMeta() {
		List<KafkaHandlerMeta> meta = new ArrayList<>();

		String[] kafkaHandlerBeanNames = applicationContext.getBeanNamesForAnnotation(KafkaHandlers.class);
		for (String kafkaHandlerBeanName : kafkaHandlerBeanNames) {
			Object kafkaHandlerBean = applicationContext.getBean(kafkaHandlerBeanName);
			Class<? extends Object> kafkaHandlerBeanClazz = kafkaHandlerBean.getClass();
			Map<Class<? extends Annotation>, Map<Method, Annotation>> mapData = extractAnnotationMaps(kafkaHandlerBeanClazz);

			meta.addAll(convertAnnotationMaps2Meta(mapData, kafkaHandlerBean));
		}

		return meta;
	}

	/**调用extractAnnotationMaps提取InputConsumer等注解
	 * @param clazz
	 * @return
	 */
	protected Map<Class<? extends Annotation>, Map<Method, Annotation>> extractAnnotationMaps(Class<? extends Object> clazz) {
		AnnotationTranversor<Class<? extends Annotation>, Method, Annotation> annotationTranversor = new AnnotationTranversor<>(clazz);

		Map<Class<? extends Annotation>, Map<Method, Annotation>> data = annotationTranversor.tranverseAnnotation(
				new AnnotationHandler<Class<? extends Annotation>, Method, Annotation>() {
					public void handleMethodAnnotation(Class<? extends Object> clazz, Method method, Annotation annotation, TranversorContext<Class<? extends Annotation>, Method, Annotation> context) {
						if (annotation instanceof InputConsumer)
							context.addEntry(InputConsumer.class, method, annotation);
						else if (annotation instanceof OutputProducer)
							context.addEntry(OutputProducer.class, method, annotation);
						else if (annotation instanceof ErrorHandler)
							context.addEntry(ErrorHandler.class, method, annotation);
					}

					public void handleClassAnnotation(Class<? extends Object> clazz, Annotation annotation, TranversorContext<Class<? extends Annotation>, Method, Annotation> context) {
						if (annotation instanceof KafkaHandlers)
							log.warn("There is some other annotation {} rather than @KafkaHandlers in the handler class {}.", annotation.getClass().getName(), clazz.getName());
					}
				});

		return data;
	}

	/**把提取出来的注解转换成元数据对象，供接下来创建相应的生产者和消费者对象使用
	 * @param mapData
	 * @param bean
	 * @return
	 */
	protected List<KafkaHandlerMeta> convertAnnotationMaps2Meta(Map<Class<? extends Annotation>, Map<Method, Annotation>> mapData, Object bean) {
		List<KafkaHandlerMeta> meta = new ArrayList<KafkaHandlerMeta>();

		Map<Method, Annotation> inputConsumerMap = mapData.get(InputConsumer.class);
		Map<Method, Annotation> outputProducerMap = mapData.get(OutputProducer.class);
		Map<Method, Annotation> exceptionHandlerMap = mapData.get(ErrorHandler.class);

		for (Map.Entry<Method, Annotation> entry : inputConsumerMap.entrySet()) {
			InputConsumer inputConsumer = (InputConsumer) entry.getValue();

			KafkaHandlerMeta kafkaHandlerMeta = new KafkaHandlerMeta();

			kafkaHandlerMeta.setBean(bean);
			kafkaHandlerMeta.setMethod(entry.getKey());

			Parameter[] kafkaHandlerParameters = entry.getKey().getParameters();
			if (kafkaHandlerParameters.length != 1)
				throw new IllegalArgumentException("The kafka handler method can contains only one parameter.");
			kafkaHandlerMeta.setParameterType(kafkaHandlerParameters[0].getType());

			kafkaHandlerMeta.setInputConsumer(inputConsumer);

			if (outputProducerMap != null && outputProducerMap.containsKey(entry.getKey()))
				kafkaHandlerMeta.setOutputProducer((OutputProducer) outputProducerMap.get(entry.getKey()));

			if (exceptionHandlerMap != null)
				for (Map.Entry<Method, Annotation> excepHandlerEntry : exceptionHandlerMap.entrySet()) {
					ErrorHandler eh = (ErrorHandler) excepHandlerEntry.getValue();
					if (StringUtils.isEmpty(eh.topic()) || eh.topic().equals(inputConsumer.topic())) {
						kafkaHandlerMeta.addErrorHandlers((ErrorHandler) eh, excepHandlerEntry.getKey());
					}
				}

			meta.add(kafkaHandlerMeta);
		}

		return meta;
	}

	/**创建kafka处理器
	 * @param kafkaHandlerMeta
	 */
	protected void createKafkaHandler(final KafkaHandlerMeta kafkaHandlerMeta) {
		Class<? extends Object> paramClazz = kafkaHandlerMeta.getParameterType();

		MyKafkaProducer kafkaProducer = createProducer(kafkaHandlerMeta);
		List<ExceptionHandler> excepHandlers = createExceptionHandlers(kafkaHandlerMeta);

		MessageHandler beanMessageHandler = null;
		if (List.class.isAssignableFrom(paramClazz)) {
			beanMessageHandler = createBeansHandler(kafkaHandlerMeta, kafkaProducer, excepHandlers);
		} else {
			beanMessageHandler = createBeanHandler(kafkaHandlerMeta, kafkaProducer, excepHandlers);
		}

		MyKafkaConsumer kafkaConsumer = createConsumer(kafkaHandlerMeta, beanMessageHandler);
		kafkaConsumer.startup();

		KafkaHandler kafkaHandler = new KafkaHandler(kafkaConsumer, kafkaProducer, excepHandlers, kafkaHandlerMeta);

		kafkaHandlers.add(kafkaHandler);
	}

	/**创建异常处理器，当InputConsumer和OutputProducer处理失败时就会调用标记ErrorHandler注解的异常处理器来处理
	 * @param kafkaHandlerMeta
	 * @return
	 */
	private List<ExceptionHandler> createExceptionHandlers(final KafkaHandlerMeta kafkaHandlerMeta) {
		List<ExceptionHandler> excepHandlers = new ArrayList<ExceptionHandler>();

		for (final Map.Entry<ErrorHandler, Method> errorHandler : kafkaHandlerMeta.getErrorHandlers().entrySet()) {
			ExceptionHandler exceptionHandler = new ExceptionHandler() {
				public boolean support(Throwable t) {
					// We handle the exception when the classes are exactly same
					return errorHandler.getKey().exception() == t.getClass();
				}

				public void handle(Throwable t, String message) {

					Method excepHandlerMethod = errorHandler.getValue();
					try {
						excepHandlerMethod.invoke(kafkaHandlerMeta.getBean(), t, message);
					} catch (IllegalAccessException e) {
						// If annotated exception handler is correct, this won't
						// happen
						log.error(
								"No permission to access the annotated exception handler.",
								e);
						throw new IllegalStateException(
								"No permission to access the annotated exception handler. Please check annotated config.",
								e);
					} catch (IllegalArgumentException e) {
						// If annotated exception handler is correct, this won't
						// happen
						log.error(
								"The parameter passed in doesn't match the annotated exception handler's.",
								e);
						throw new IllegalStateException(
								"The parameter passed in doesn't match the annotated exception handler's. Please check annotated config.",
								e);
					} catch (InvocationTargetException e) {
						// If the exception during handling exception occurs,
						// throw it, in SafelyMessageHandler, this will be
						// processed
						log.error(
								"Failed to call the annotated exception handler.",
								e);
						throw new IllegalStateException(
								"Failed to call the annotated exception handler. Please check if the handler can handle the biz without any exception.",
								e);
					}
				}
			};

			excepHandlers.add(exceptionHandler);
		}

		return excepHandlers;
	}

	@SuppressWarnings("unchecked")
	protected BeanMessageHandler<Object> createBeanHandler(final KafkaHandlerMeta kafkaHandlerMeta, final MyKafkaProducer MyKafkaProducer, List<ExceptionHandler> excepHandlers) {

		// We have to abandon the type check
		@SuppressWarnings("rawtypes")
		BeanMessageHandler beanMessageHandler = new BeanMessageHandler(kafkaHandlerMeta.getParameterType(), excepHandlers) {
			@Override
			protected void doExecuteBean(Object bean) {
				invokeHandler(kafkaHandlerMeta, MyKafkaProducer, bean);
			}
		};

		return beanMessageHandler;
	}

	@SuppressWarnings("unchecked")
	protected BeansMessageHandler<Object> createBeansHandler(final KafkaHandlerMeta kafkaHandlerMeta, final MyKafkaProducer MyKafkaProducer, List<ExceptionHandler> excepHandlers) {

		// We have to abandon the type check
		@SuppressWarnings("rawtypes")
		BeansMessageHandler beanMessageHandler = new BeansMessageHandler(kafkaHandlerMeta.getParameterType(), excepHandlers) {
			@Override
			protected void doExecuteBeans(List bean) {
				invokeHandler(kafkaHandlerMeta, MyKafkaProducer, bean);
			}
		};

		return beanMessageHandler;
	}

	protected MyKafkaProducer createProducer(final KafkaHandlerMeta kafkaHandlerMeta) {
		MyKafkaProducer kafkaProducer = null;

		if (kafkaHandlerMeta.getOutputProducer() != null) {
			kafkaProducer = new MyKafkaProducer(kafkaHandlerMeta.getOutputProducer().propertiesFile(), kafkaHandlerMeta.getOutputProducer().defaultTopic());
		}

		// It may return null，因为可能方法上没有@OutputProducer注解
		return kafkaProducer;
	}

	private void invokeHandler(final KafkaHandlerMeta kafkaHandlerMeta, final MyKafkaProducer kafkaProducer, Object parameter) {
		Method kafkaHandlerMethod = kafkaHandlerMeta.getMethod();
		try {
			Object result = kafkaHandlerMethod.invoke(kafkaHandlerMeta.getBean(), parameter);

			if (kafkaProducer != null) {
				kafkaProducer.send(JacksonUtil.ObjecttoJSon(result));
			}
		} catch (IllegalAccessException e) {
			// If annotated config is correct, this won't happen
			log.error("No permission to access the annotated kafka handler.", e);
			throw new IllegalStateException(
					"No permission to access the annotated kafka handler. Please check annotated config.",
					e);
		} catch (IllegalArgumentException e) {
			// If annotated config is correct, this won't happen
			log.error(
					"The parameter passed in doesn't match the annotated kafka handler's.",
					e);
			throw new IllegalStateException(
					"The parameter passed in doesn't match the annotated kafka handler's. Please check annotated config.",
					e);
		} catch (InvocationTargetException e) {
			// The SafeMessageHanlder has already handled the
			// throwable, no more exception goes here
			log.error("Failed to call the annotated kafka handler.", e);
			throw new IllegalStateException(
					"Failed to call the annotated kafka handler. Please check if the handler can handle the biz without any exception.",
					e);
		}
	}

	protected MyKafkaConsumer createConsumer(final KafkaHandlerMeta kafkaHandlerMeta, MessageHandler beanMessageHandler) {
		MyKafkaConsumer kafkaConsumer = null;

		if (kafkaHandlerMeta.getInputConsumer().fixedThreadNum() > 0) {
			kafkaConsumer = new MyKafkaConsumer(kafkaHandlerMeta
					.getInputConsumer().propertiesFile(), kafkaHandlerMeta
					.getInputConsumer().topic(), kafkaHandlerMeta
					.getInputConsumer().streamNum(), kafkaHandlerMeta
					.getInputConsumer().fixedThreadNum(), beanMessageHandler);

		} else if (kafkaHandlerMeta.getInputConsumer().maxThreadNum() > 0
				&& kafkaHandlerMeta.getInputConsumer().minThreadNum() < kafkaHandlerMeta
						.getInputConsumer().maxThreadNum()) {
			kafkaConsumer = new MyKafkaConsumer(kafkaHandlerMeta
					.getInputConsumer().propertiesFile(), kafkaHandlerMeta
					.getInputConsumer().topic(), kafkaHandlerMeta
					.getInputConsumer().streamNum(), kafkaHandlerMeta
					.getInputConsumer().minThreadNum(), kafkaHandlerMeta
					.getInputConsumer().maxThreadNum(), beanMessageHandler);

		} else {
			kafkaConsumer = new MyKafkaConsumer(kafkaHandlerMeta
					.getInputConsumer().propertiesFile(), kafkaHandlerMeta
					.getInputConsumer().topic(), kafkaHandlerMeta
					.getInputConsumer().streamNum(), beanMessageHandler);
		}

		return kafkaConsumer;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	// The consumers can't be shutdown automatically by jvm shutdown hook if
	// this method is not called
	public void shutdownAll() {
		for (KafkaHandler kafkahandler : kafkaHandlers) {
			kafkahandler.getKafkaConsumer().shutdownGracefully();

			kafkahandler.getKafkaProducer().close();
		}
	}

	public List<KafkaHandler> getKafkaHandlers() {
		return kafkaHandlers;
	}

	public void setKafkaHandlers(List<KafkaHandler> kafkaHandlers) {
		this.kafkaHandlers = kafkaHandlers;
	}
}