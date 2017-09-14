package com.productiveanalytics.rabbitmq_spring_int.config.lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rabbitmq.client.Channel;

import org.springframework.amqp.rabbit.connection.ChannelListener;

/**
 * Lifecycle Listener for Channel
 * 
 * @author LChawathe
 */

public class ChannelLifeCycleListener implements ChannelListener
{

	private static final Log logger = LogFactory.getLog(ChannelLifeCycleListener.class);
	
	public void onCreate(Channel channel, boolean transactional) {
		logger.info("New rabbitMQ channel created" + channel);
		channel.addShutdownListener(new ChannelShutDownListener());
		ChannelLifeCycleTracker.addChannel(channel.getChannelNumber(), channel);
	}

}
