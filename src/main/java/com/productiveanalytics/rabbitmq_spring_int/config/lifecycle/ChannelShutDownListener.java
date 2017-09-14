package com.productiveanalytics.rabbitmq_spring_int.config.lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * @author LChawathe
 */

public class ChannelShutDownListener implements ShutdownListener
{
	private static final Log logger = LogFactory.getLog(ChannelShutDownListener.class);

	public void shutdownCompleted(ShutdownSignalException cause) {
		logger.info("Channel shutdown completed " + cause.getReason() + cause.getLocalizedMessage());
		try {
			Channel channel = (Channel) cause.getReference();
			Integer channelNumber = channel.getChannelNumber();
			ChannelLifeCycleTracker.removeChannel(channelNumber);
			logger.info("Channel removed from ChannelLifeCycleTracker, channel id : "  + channelNumber);
		} catch (ShutdownSignalException ex) {
			logger.error("Exception occured in shutdown listener", ex);
		}
	}
}