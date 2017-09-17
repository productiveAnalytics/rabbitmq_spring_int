package com.productiveanalytics.rabbitmq_spring_int.constants;

public final class RabbitMQSpringConstants 
{
	private RabbitMQSpringConstants() {
		/* Do not allow instantiation */
	}
	
	public static final String PROP_HOST 			= "host";
	public static final String PROP_VIRTUAL_HOST 	= "virtualHost";
	
	public static final String PROP_USER_NAME 		= "username";
	public static final String PROP_PASSWORD  		= "password";
	
	public static final String PROP_EXCHANGE_NAME 	= "exchangeName";
	public static final String PROP_QUEUE_NAME	  	= "queueName";
}
