package com.productiveanalytics.rabbitmq_spring_int.listener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.MessageConverter;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import org.springframework.util.Assert;

import com.productiveanalytics.rabbitmq_spring_int.constants.RabbitMQSpringConstants;
import com.productiveanalytics.rabbitmq_spring_int.model.CustomRequest;
import com.rabbitmq.client.Channel;

/**
 * TWO ways of handling messages: explicit MessageListener to handle incoming message or annotated @RabbitListener method
 * 
 * The corresponding XML looks like:
 *  
 *	  <bean id="myMessageListener" 
 * 		    class="org.hanbo.amqp.sample.RabbitMessageListener" />
 *	  <bean id="defaultMessageConverter"
 * 		    class="org.springframework.amqp.support.converter.JsonMessageConverter" />
 * 
 * 	  <rabbit:listener-container
 * 			connection-factory="rabbitmqConnectionFactory"
 * 			acknowledge="auto"
 * 	   		requeue-rejected="true"
 * 			message-converter="defaultMessageConverter">
 * 		 <rabbit:listener 
 * 			ref="myMessageListener" 
 * 			method="handleIncoming"
 * 			queues="testQueue"/>
 *    </rabbit:listener-container>
 * 
 * @author LChawathe
 *
 */

@Configuration
@EnableRabbit	/* @EnableRabbit enables detection of @RabbitListener annotations on any Spring-managed bean in the container. */
public class RabbitMQMessageListener 
		implements InitializingBean
{
	private static final int DEFAULT_MAX_CONCURRENT_CONSUMERS_COUNT = 1;

	@Resource
	private Properties rabbitMQProperties;
	
	@Resource
	private ConnectionFactory rabbitmqConnectionFactory;
	
	@Autowired
	private ChannelAwareMessageListener messageListener;
	
	@Resource
	private MessageConverter defaultMessageConverter;
	
	/**
	 * METHOD # 1
	 * MessageListener or sophisticated ChannelAwareMessageListener act as callback to handle received message
	 */
	@Bean(name="messageListener")
    public ChannelAwareMessageListener getChannelAwareMessageListener()
    {
		return new MyChannelAwareMessageListener(defaultMessageConverter);
    }
	
	@Bean(name="messageListenerContainer")
	public SimpleMessageListenerContainer getMessageListenerContainer()
	{
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    	container.setConnectionFactory(rabbitmqConnectionFactory);
    	String queueName	= rabbitMQProperties.getProperty(RabbitMQSpringConstants.PROP_QUEUE_NAME);
    	container.setQueueNames(queueName);
    	container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
    	
    	final int maxConcurrentConsumersCount = DEFAULT_MAX_CONCURRENT_CONSUMERS_COUNT;
    	container.setMaxConcurrentConsumers(maxConcurrentConsumersCount);
    	
    	// Use the callback to handle incoming messages
    	container.setMessageListener(messageListener);
    	
    	container.setMessageConverter(defaultMessageConverter);
    	
    	return container;
	}
	
	@Bean(name="rabbitListenerContainerFactory")
	@Scope("singleton")
    public SimpleRabbitListenerContainerFactory getRabbitListenerContainerFactory() {
      SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory = new SimpleRabbitListenerContainerFactory();
      rabbitListenerContainerFactory.setConnectionFactory(rabbitmqConnectionFactory);
      rabbitListenerContainerFactory.setMaxConcurrentConsumers(DEFAULT_MAX_CONCURRENT_CONSUMERS_COUNT);
      return rabbitListenerContainerFactory;
    }
	
	/**
	 * METHOD # 2 : @RabbitListener to process the messages from the queue
	 */
//	@RabbitListener(containerFactory="rabbitListenerContainerFactory"
//				   ,queues="#{myQueue.name}")	/*	Using Spring Expression Language (SpEL). Reference queueName bean's name property. */ 
//	public void processMessage(Message msg) {
//		System.out.println("Received @ "+ RabbitMQSpringConstants.DATE_FORMAT.format(new Date()) +": "+ msg);
//	}
	
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(rabbitMQProperties, "RabbitMQ Properties cannot be null!"); 
		Assert.notNull(rabbitmqConnectionFactory, "RabbitMQ Connection Factory cannot be null!");
		Assert.notNull(defaultMessageConverter, "Message Converter cannot be null!");
	}
	
	private static class MyChannelAwareMessageListener
						 implements ChannelAwareMessageListener
	{
		private final boolean multiple = false;
		private final boolean requeue = true;
		
		private MessageConverter messageConverter;
		private Map<Integer, Integer> processingFailureCountMap = new HashMap<Integer, Integer>();
		
		MyChannelAwareMessageListener(MessageConverter msgConverter) {
			this.messageConverter = msgConverter;
		}
		
		private boolean processMessage(Message message)
		{
			CustomRequest req = (CustomRequest) this.messageConverter.fromMessage(message);
			
			Assert.notNull(req, "Parsing of incoming message is unsuccessful");
			
			int objId = req.getId();
			
			if (objId < 0) {
				Integer key = new Integer(objId);
				int failureCount = 0;
				if (!processingFailureCountMap.containsKey(key)) {
					processingFailureCountMap.put(key, new Integer(0));
				} 
				failureCount = processingFailureCountMap.get(key);
				++failureCount;
				processingFailureCountMap.put(key, failureCount);
				
				// To break infinite re-queueing on Exception, after 3 failures, provide NACK to allow de-queuing.
				if (failureCount <= 3)
					throw new RuntimeException("Simulating the Exception with -ve Id");
				else {
					System.out.println("Send NACK and STOP processing for "+ objId);
					return false;
				}
			}
			
			// Simulate complex processing that may fail sometimes
			return (objId % 2 == 1);
		}
		
		public void onMessage(Message message, Channel channel) throws Exception 
		{
			long deliveryTag = message.getMessageProperties().getDeliveryTag();
			
			System.out.println("Channel#"+ channel.getChannelNumber() +"->Received @ "+ RabbitMQSpringConstants.DATE_FORMAT.format(new Date()) +": "+ message);
			
			try 
			{
				if (processMessage(message))
				{
					System.out.println("+++++++++> ACK : Channel#"+ channel.getChannelNumber() +" Message: "+ message);
					
					// Successful processing, so Acknowledge
					channel.basicAck(deliveryTag, multiple);
				}
				else
				{
					System.out.println("---------> NACK: Channel#"+ channel.getChannelNumber() +" Message: "+ message);
					
					// Received the message, and processing, though not successfully. So do Negative Acknowledgement.
					channel.basicNack(deliveryTag, multiple, false /* requeue */);
				}
			}
			catch (Exception ex) {
				System.err.println("---------> NACK : Channel#"+ channel.getChannelNumber() +" Message: "+ message);
				channel.basicNack(deliveryTag, multiple, requeue);
			}
		}
	}
}
