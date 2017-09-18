package com.productiveanalytics.rabbitmq_spring_int.service;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.productiveanalytics.rabbitmq_spring_int.config.utils.RabbitMQUtility;
import com.productiveanalytics.rabbitmq_spring_int.model.CustomRequest;

@Component("messagingService")
@Scope("singleton")
public class MessagingServiceImpl implements MessagingService, InitializingBean
{
	private static final Log logger = LogFactory.getLog(MessagingServiceImpl.class);
	
	@Resource(name="rabbitmqTemplate")
	private RabbitTemplate rabbitmqTemplate;
	
	@Resource(name="messageListenerContainer")
	private SimpleMessageListenerContainer messageListenerContainer;
	
	private MessageConverter messageConverter;
	/*
	 * Using setter injection, instead of @Autowired MessageConverter messageConverter;
	 */
	@Autowired @Qualifier("defaultMessageConverter")
	public void setMessageConverter(MessageConverter messageConverter) {
		this.messageConverter = messageConverter;
	}
	
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(rabbitmqTemplate, "RabbitMQ Template is null. FATAL error!");
		Assert.notNull(messageConverter, "Message Converted is null. FATAL error!");
		Assert.notNull(messageListenerContainer, "RabbitMQ/Spring Integration message listener container is null. FATAL Error!");
		
		/*
		 * Start the message listener container, if not running.
		 */
		boolean startStatus = RabbitMQUtility.startMessageListenerContainer(messageListenerContainer);
		if (startStatus) {
			logger.debug("[MessagingService] RabbitMQ/Spring Integration message listener container is running");
		}
	}

	/**
	 * Producer for sending messages to Exchange/Queue
	 */
	public boolean sendMessageToRabbitMQ(CustomRequest requestObject)
	{
		MessageProperties props = MessagePropertiesBuilder.newInstance()
				.setContentType(MessageProperties.CONTENT_TYPE_JSON)
				.setHeader("TYPE", requestObject.getType())
				.build();
		
		Message msg = null;
		try {
			msg = messageConverter.toMessage(requestObject, props);
		} catch (MessageConversionException msgConvrEx) {
			logger.error("Error while conversion", msgConvrEx);
			return Boolean.FALSE;
		}
		
		try {
			rabbitmqTemplate.send(msg);
			return Boolean.TRUE;
		} catch (AmqpException amqpEx) {
			logger.error("Error while sending message:"+ msg , amqpEx);
			return Boolean.FALSE;
		}
	}

}
