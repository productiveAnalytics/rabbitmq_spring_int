package com.productiveanalytics.rabbitmq_spring_int.service;

import javax.annotation.Resource;
import javax.jws.WebService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import com.productiveanalytics.rabbitmq_spring_int.config.utils.RabbitMQUtility;
import com.productiveanalytics.rabbitmq_spring_int.constants.RabbitMQSpringConstants;

/**
 * @author LChawathe
 *
 */
@WebService(endpointInterface="com.productiveanalytics.rabbitmq_spring_int.service.ListenerManagementWS",
			targetNamespace="http://www.productiveAnalytics.com/RabbitMQ_Sprint_int",
			serviceName="ListenerManagementWS")
public class ListenerManagementWSImpl implements ListenerManagementWS{

	private static final Log logger = LogFactory.getLog(ListenerManagementWSImpl.class);
	
	@Resource(name="messageListenerContainer")
	private SimpleMessageListenerContainer messageListenerContainer;
	
	/**
	 * Toggle message listener container state
	 */
	public String changeListenerStatus(String listenerName){
		String responseString = "Failed to perform requested maintenance task";
		try{
			if(RabbitMQSpringConstants.LISTENER_NAME.equals(listenerName))
			{
				if(messageListenerContainer.isRunning()){
					logger.info("Stopping the RabbitMQ/Spring Integration message listener container");
					messageListenerContainer.stop();
					
					if (!messageListenerContainer.isRunning()){
						responseString = "RabbitMQ/Spring Integration message listener container stopped succesfully";
					}
				}else{
					boolean restartStatus = RabbitMQUtility.startMessageListenerContainer(messageListenerContainer);
					
					if (restartStatus) {
						responseString ="RabbitMQ/Spring Integration message listener container started succesfully";
					}
				}
			}
		}catch(Exception ex){
			logger.error("Failed to process listener management request", ex);
		}
		return responseString;
	}

	public String getListenerStatus(String listenerName) {
		Boolean listnrStatus = Boolean.FALSE;
		try{
			if (RabbitMQSpringConstants.LISTENER_NAME.equals(listenerName)){
				if (messageListenerContainer.isRunning()){
					listnrStatus = Boolean.TRUE;
				}
			}
		}catch(Exception ex){
			logger.error("Failed to process listener container management request", ex);
		}
		return listnrStatus.toString();
	}

}
