package com.productiveanalytics.rabbitmq_spring_int.exceptions;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ExceptionHandler;
import com.rabbitmq.client.TopologyRecoveryException;


/**
 * @author LChawathe
 *
 */
public class GenericExceptionHandler implements ExceptionHandler {

	private static final Log logger = LogFactory.getLog(GenericExceptionHandler.class);
	
	public void handleBlockedListenerException(Connection connection, Throwable exception) {
		logger.error("handleBlockedListenerException", exception);
		handleConnectionKiller(connection, exception, "BlockedListener");
	}

	public void handleChannelRecoveryException(Channel arg0, Throwable exception) {
		logger.error("handleChannelRecoveryException", exception);
		
	}

	public void handleConfirmListenerException(Channel channel, Throwable exception) {
		logger.error("handleConfirmListenerException", exception);
		handleChannelKiller(channel, exception, "ConfirmListener.handle{N,A}ck");
		
	}

	public void handleConnectionRecoveryException(Connection arg0, Throwable exception) {
		logger.error("handleConnectionRecoveryException", exception);
		
	}

	public void handleConsumerException(Channel channel, Throwable exception, Consumer consumer, String consumerTag, String methodName) {
		logger.error("handleConsumerException", exception);
		handleChannelKiller(channel, exception, "Consumer " + consumer
                + " (" + consumerTag + ")"
                + " method " + methodName
                + " for channel " + channel);
	}

	public void handleFlowListenerException(Channel channel, Throwable exception) {
		logger.error("handleFlowListenerException", exception);
		 handleChannelKiller(channel, exception, "FlowListener.handleFlow");
	}

	public void handleReturnListenerException(Channel channel, Throwable exception) {
		logger.error("handleReturnListenerException", exception);
		handleChannelKiller(channel, exception, "ReturnListener.handleReturn");
	}

	public void handleTopologyRecoveryException(Connection arg0, Channel arg1, TopologyRecoveryException exception) {
		logger.error("handleTopologyRecoveryException", exception);
		
	}

	public void handleUnexpectedConnectionDriverException(Connection connection, Throwable exception) {
		logger.error("handleUnexpectedConnectionDriverException", exception);
		handleConnectionKiller(connection, exception, "UnexpectedConnectionDriverException");
	}
	
    protected void handleChannelKiller(Channel channel, Throwable exception, String what) {
    	logger.error("DefaultExceptionHandler: " + what + " threw an exception for channel "  + channel + ":");
        logger.error(exception);
        try {
            channel.close(AMQP.REPLY_SUCCESS, "Closed due to exception from " + what);
        } catch (AlreadyClosedException ace) {
            // nothing to do
        	logger.error(ace);
        } catch (IOException ioe) {
        	logger.error("Failure during close of channel " + channel + " after " + exception + ":");
        	logger.error(ioe);
            channel.getConnection().abort(AMQP.INTERNAL_ERROR, "Internal error closing channel for " + what);
        } catch (TimeoutException toEx) {
        	logger.error("Failure during close of channel " + channel + " after " + exception + ":");
        	logger.error(toEx);
        	channel.getConnection().abort(AMQP.INTERNAL_ERROR, "Internal error closing channel for " + what);
		}
    }
    
    protected void handleConnectionKiller(Connection connection, Throwable exception, String what) {
    	logger.error("DefaultExceptionHandler: " + what + " threw an exception for connection "  + connection + ":");
        logger.error(exception);
        try {
            connection.close(AMQP.REPLY_SUCCESS, "Closed due to exception from " + what);
        } catch (AlreadyClosedException ace) {
            // nothing to do
        	logger.error(ace);
        } catch (IOException ioe) {
        	logger.error("Failure during close of connection " + connection + " after " + exception + ":");
        	logger.error(ioe);
            connection.abort(AMQP.INTERNAL_ERROR, "Internal error closing connection for " + what);
        }
    }

}
