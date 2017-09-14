package com.productiveanalytics.rabbitmq_spring_int.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.productiveanalytics.rabbitmq_spring_int.config.lifecycle.ChannelLifeCycleListener;
import com.productiveanalytics.rabbitmq_spring_int.exceptions.GenericExceptionHandler;

/**
 * 
 * @author LChawathe
 *
 */

@Configuration
public class CommonBeanConfigurations
{

	private static final String PROP_HOST = "host";
	private static final String PROP_VIRTUAL_HOST = "virtualHost";
	
	private static final String PROP_USER_NAME = "username";
	private static final String PROP_PASSWORD  = "password";
	
	private static final String PROP_EXCHANGE_NAME = "exchangeName";
	private static final String PROP_QUEUE_NAME	  = "queueName";
	
	private static final String RABBIT_PROPERTIES  = System.getProperty("rabbitmq.properties");
	
	private static final String DEFAULT_PROPERTY_FILE_NAME = "default_rabbit_mq.properties";
	
	@Bean(name="rabbitMQProperties") 
	@Scope("singleton")
	public Properties getRabbitMQProperties() 
					  throws IOException
	{
		Properties properties = new Properties();
		
		String rabbitPropertyFilename;
		
		if (RABBIT_PROPERTIES != null && RABBIT_PROPERTIES.trim().length() > 0) {
			rabbitPropertyFilename = RABBIT_PROPERTIES;
		} else {
			System.out.println("Using default file "+ DEFAULT_PROPERTY_FILE_NAME);
			rabbitPropertyFilename = DEFAULT_PROPERTY_FILE_NAME;
		}
		
		InputStream inputStream = new FileInputStream(rabbitPropertyFilename);
		properties.load(inputStream);
		return properties;
	}
	
	@Bean(name="rabbitmqConnectionFactory")
	@Scope("singleton")
    public org.springframework.amqp.rabbit.connection.ConnectionFactory getRabbitMQConnectionFactory()
    																	throws IOException 
	{
		com.rabbitmq.client.ConnectionFactory factory = new com.rabbitmq.client.ConnectionFactory();
		factory.setRequestedHeartbeat(10);
    	factory.setVirtualHost(getRabbitMQProperties().getProperty(PROP_VIRTUAL_HOST));
    	factory.setUsername(getRabbitMQProperties().getProperty(PROP_USER_NAME));
    	factory.setPassword(getRabbitMQProperties().getProperty(PROP_PASSWORD));
    	factory.setAutomaticRecoveryEnabled(true);
    	factory.setExceptionHandler(new GenericExceptionHandler());
    	factory.setNetworkRecoveryInterval(10000);
    	
    	CachingConnectionFactory connectionFactory = new CachingConnectionFactory(factory);
    	connectionFactory.setRequestedHeartBeat(10);
    	connectionFactory.setAddresses(getRabbitMQProperties().getProperty(PROP_HOST));
    	connectionFactory.addChannelListener(new ChannelLifeCycleListener());
    	return connectionFactory;
	}
	
	@Bean(name="rabbitmqTemplate")
	@Scope("singleton")
	public RabbitTemplate getAMQPTemplate() throws IOException 
	{
		RabbitTemplate template = new RabbitTemplate();
		template.setConnectionFactory(getRabbitMQConnectionFactory());
		template.setExchange(getRabbitMQProperties().getProperty(PROP_EXCHANGE_NAME));
		template.setRoutingKey(getRabbitMQProperties().getProperty(PROP_QUEUE_NAME));
		template.setQueue(getRabbitMQProperties().getProperty(PROP_QUEUE_NAME));
		
		RetryTemplate retryTemplate = new RetryTemplate();
		
		ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
		backOffPolicy.setInitialInterval(500);
		backOffPolicy.setMultiplier(10.0);
		backOffPolicy.setMaxInterval(10000);
		
		retryTemplate.setBackOffPolicy(backOffPolicy);
		
		template.setRetryTemplate(retryTemplate);
		return template;	
	}

}
