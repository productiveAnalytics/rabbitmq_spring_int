package com.productiveanalytics.rabbitmq_spring_int.config;

import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;

import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;

import org.springframework.util.Assert;

import com.productiveanalytics.rabbitmq_spring_int.config.utils.RabbitMQUtility;
import com.productiveanalytics.rabbitmq_spring_int.constants.RabbitMQSpringConstants;

/**
 * This initializes the Exchance, Queue, etc. 
 * 
 * @author LChawathe
 *
 */

@Configuration
@Order(value=3)
public class RabbitMQBootstrap implements InitializingBean
{
	private static final Log logger = LogFactory.getLog(RabbitMQBootstrap.class);
	
	private static boolean durable    = true;  //durable - RabbitMQ will never lose the exchange/queue if a crash occurs
	private static boolean autoDelete = false; //autodelete - exchange/queue is deleted when last consumer unsubscribes
	
	@Autowired
	@Qualifier("rabbitMQProperties")	
	private Properties rabbitmqProps;
	
	@Autowired
	private AmqpAdmin rabbitmqAdmin;
	
	@Autowired
	@Qualifier("messageListenerContainer")
	private SimpleMessageListenerContainer messageListenerContainer;
		
	@Autowired
	@Qualifier("myQueue")
	private Queue myQueue;
	
	@Autowired
	@Qualifier("myDirectExchange")
	private DirectExchange myDirectExchange;

	/**
	 * Declare the Queue
	 */
	@Bean
    public Queue myQueue() {

    	boolean exclusive  = false; //exclusive - if queue only will be used by one connection

		String queueName	= rabbitmqProps.getProperty(RabbitMQSpringConstants.PROP_QUEUE_NAME);
		
		System.err.println("Establishing Queue: *********"+ queueName +"*********");
		
		Queue newQ = new Queue(queueName, durable, exclusive, autoDelete);
		rabbitmqAdmin.declareQueue(newQ);
		
        return newQ;
    }
	
	/**
	 * Declare the Exchange
	 */
	@Bean
	public Exchange myDirectExchange() {
		String exchangeName	= rabbitmqProps.getProperty(RabbitMQSpringConstants.PROP_EXCHANGE_NAME);
		
		System.err.println("Establishing Exchange: *********"+ exchangeName +"*********");
		
		Exchange xchng = new DirectExchange(exchangeName, durable, autoDelete);
		rabbitmqAdmin.declareExchange(xchng);
		
		return xchng;
	}
	
	/**
	 * 
	 * This is very important to create binding only after let Spring declare Queue & Exchange.
	 * This will avoid issue "Requested bean is currently in creation: Is there an unresolvable circular reference?"
	 * 
	 */
	@PostConstruct
	public Binding myDirectExchangeToQueueBinding()
	{
		// Routing Key has been kept same as Queue Name
		String routingKey	= rabbitmqProps.getProperty(RabbitMQSpringConstants.PROP_QUEUE_NAME);
		
		Assert.isInstanceOf(DirectExchange.class, myDirectExchange, "Check the Exchange type. Must be Direct exchange fo this binding");
		
		System.err.println("Bound (Direct) Exchange: "+ myDirectExchange.getName() + " to Queue: "+ myQueue.getName());
		
		Binding routingKeyBasedBinding = BindingBuilder.bind(this.myQueue)
													   .to(this.myDirectExchange)
													   .with(routingKey /* designed to be same as QueueName */);
		rabbitmqAdmin.declareBinding(routingKeyBasedBinding);
		
		return routingKeyBasedBinding;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(messageListenerContainer, "RabbitMQ/Spring Integration message listener container is null. FATAL Error!");
		
		boolean startStatus = RabbitMQUtility.startMessageListenerContainer(messageListenerContainer);
		if (startStatus) {
			logger.debug("[RabbitMQBootstrap] RabbitMQ/Spring Integration message listener container is running");
			System.err.println("[RabbitMQBootstrap] RabbitMQ/Spring Integration message listener container is running");
		}
	}
}
