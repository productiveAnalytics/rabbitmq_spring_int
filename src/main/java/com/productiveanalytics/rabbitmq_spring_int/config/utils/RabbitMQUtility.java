package com.productiveanalytics.rabbitmq_spring_int.config.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;

public final class RabbitMQUtility 
{
	private static Log logger = LogFactory.getLog(RabbitMQUtility.class); 
	
	public static boolean startMessageListenerContainer(MessageListenerContainer msgListenerContainer)
	{
		if (msgListenerContainer == null) return Boolean.FALSE;
		
		if (!msgListenerContainer.isRunning())
		{
			logger.info("Starting the RabbitMQ/Spring Integration message listener container");
			msgListenerContainer.start();
			
			if (msgListenerContainer.isRunning()){
				logger.debug("RabbitMQ/Spring Integration message listener container started succesfully");
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		} else {
			logger.info("RabbitMQ/Spring Integration message listener container is already running. Skipping...");
			return Boolean.TRUE;
		}
	}
}
