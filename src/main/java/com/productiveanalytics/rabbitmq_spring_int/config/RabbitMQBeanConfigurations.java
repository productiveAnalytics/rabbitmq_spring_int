package com.productiveanalytics.rabbitmq_spring_int.config;

import java.io.IOException;
import java.util.Properties;

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import org.springframework.core.annotation.Order;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

import com.productiveanalytics.rabbitmq_spring_int.config.lifecycle.ChannelLifeCycleListener;
import com.productiveanalytics.rabbitmq_spring_int.constants.RabbitMQSpringConstants;
import com.productiveanalytics.rabbitmq_spring_int.exceptions.GenericExceptionHandler;

/**
 * Must be initialized after CommonBeanConfigurations to allow @Autowired properties initialized
 * @author LChawathe
 *
 */

@Configuration
@Order(value=2)
public class RabbitMQBeanConfigurations
			 implements InitializingBean
{
		
	/*
	 * @Resource is from JavaX
	 * Precedence : Match by Name > Match by Type > Match by Qualifier
	 */
//	@Resource
		/* 
		 * Note: As there no matching beanName for field "rabbitmqProps", just @Resource -- without name attr -- WILL CAUSE NoSuchBeanDefinitionException.
		 * 		 Solution: Must use either @Resource(name="rabbitMQProperties") 
		 * 				   OR
		 * 				   @Autowired w/ @Qualifier("rabbitMQProperties")
		 */
//	@Resource(name="rabbitMQProperties") 
//	Properties rabbitmqProps;
	/*
	 * @Autowired is from Spring framework, and it has same behavior as JavaX's @Inject
	 * Precedence : Match by Type > Match by Qualifier > Match by Name
	 */
	@Autowired
	@Qualifier("rabbitMQProperties")	
	Properties rabbitmqProps;
	
	
//	@Resource 
		/* 
		 * Note: Using @Resource to refer Bean -- defined inside same Configuration -- by another Bean from this Configuration, WILL cause NPE.
		 * 		 Solution: Use @Autowired 
		 */
	@Autowired
	org.springframework.amqp.rabbit.connection.ConnectionFactory rabbitmqConnectionFactory;
	
	public void afterPropertiesSet() throws Exception {
		/*
		 * By making CommonBeanConfigurations as @Order(1) and this bean as @Order(2), required auto-wiring will happen 
		 */
		Assert.notNull(rabbitmqProps, "RabbitMQ Properties cannot be null, FATAL error!!!");
		System.err.println("RabbitMQBeanConfigurations initialized >>>");
	}

	@Bean(name="rabbitmqConnectionFactory")
	@Scope("singleton")
    public org.springframework.amqp.rabbit.connection.ConnectionFactory getRabbitMQConnectionFactory()
    																	throws IOException 
	{
		com.rabbitmq.client.ConnectionFactory factory = new com.rabbitmq.client.ConnectionFactory();
		
		factory.setRequestedHeartbeat(10);
    	factory.setVirtualHost(rabbitmqProps.getProperty(RabbitMQSpringConstants.PROP_VIRTUAL_HOST));
    	factory.setUsername(rabbitmqProps.getProperty(RabbitMQSpringConstants.PROP_USER_NAME));
    	factory.setPassword(rabbitmqProps.getProperty(RabbitMQSpringConstants.PROP_PASSWORD));
    	factory.setAutomaticRecoveryEnabled(true);
    	factory.setExceptionHandler(new GenericExceptionHandler());
    	factory.setNetworkRecoveryInterval(10000);
    	
    	CachingConnectionFactory connectionFactory = new CachingConnectionFactory(factory);
    	connectionFactory.setRequestedHeartBeat(10);
    	connectionFactory.setAddresses(rabbitmqProps.getProperty(RabbitMQSpringConstants.PROP_HOST));
    	connectionFactory.addChannelListener(new ChannelLifeCycleListener());
    	return connectionFactory;
	}
	
	@Bean(name="rabbitmqTemplate")
	@Scope("singleton")
	public RabbitTemplate getAMQPTemplate() throws IOException 
	{
		RabbitTemplate template = new RabbitTemplate();
		
		/*
		 * Instead of calling pure Java method getRabbitMQConnectionFactory(), 
		 * use the reference Bean to allow Spring to manage dependency.
		 */
		template.setConnectionFactory(/* getRabbitMQConnectionFactory() */ rabbitmqConnectionFactory);
		
		template.setExchange(rabbitmqProps.getProperty(RabbitMQSpringConstants.PROP_EXCHANGE_NAME));
		template.setRoutingKey(rabbitmqProps.getProperty(RabbitMQSpringConstants.PROP_QUEUE_NAME));
		template.setQueue(rabbitmqProps.getProperty(RabbitMQSpringConstants.PROP_QUEUE_NAME));
		
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
