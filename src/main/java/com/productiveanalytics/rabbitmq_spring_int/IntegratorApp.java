package com.productiveanalytics.rabbitmq_spring_int;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;

import com.productiveanalytics.rabbitmq_spring_int.config.BeanConfigurationsLoader;
import com.productiveanalytics.rabbitmq_spring_int.constants.RabbitMQSpringConstants;
import com.productiveanalytics.rabbitmq_spring_int.model.CustomRequest;
import com.productiveanalytics.rabbitmq_spring_int.service.MessagingService;

public class IntegratorApp {

	public static void main(String[] args) {
		IntegratorApp app = new IntegratorApp();
		try {
			app.testSpringRabbitMQIntegration();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Note that this class is not Spring managed class, 
	 *    so even after @Autowired, the field would not be initialized.
	 *    Solution: Must get the Context and get the bean from the Spring Context
	 */
//	@Autowired
	Properties rabbitMQProperties;
	ConnectionFactory rabbitmqConnectionFactory;
	
	private IntegratorApp() {}
	
	private void testSpringRabbitMQIntegration() throws InterruptedException
	{
		BeanConfigurationsLoader loader = BeanConfigurationsLoader.getInstance();
		ApplicationContext appCtx = loader.getApplicationContext();
		rabbitMQProperties = appCtx.getBean("rabbitMQProperties", Properties.class);
		rabbitmqConnectionFactory = appCtx.getBean("rabbitmqConnectionFactory", ConnectionFactory.class);
		
		assert(rabbitMQProperties != null);
		System.out.println(rabbitMQProperties);
		
		CustomRequest reqObj = null;
		List<CustomRequest> reqList = new ArrayList<CustomRequest>();
		
		final String type = "SimpleRequest";
		
		reqObj = new CustomRequest(1, "One", new Integer(1), type, "first msg");
		reqList.add(reqObj);
		
		reqObj = new CustomRequest(2, "Two", "Dos", type, "2nd msg.");
		reqList.add(reqObj);
		
		reqObj = new CustomRequest(3, "Three", new Integer(3), type, "This is third msg");
		reqList.add(reqObj);
		
		reqObj = new CustomRequest(4, "4", "Marathi:Chaar", type, "Fourth message");
		reqList.add(reqObj);
		
		reqObj = new CustomRequest(5, "FIVE", Boolean.TRUE, type, "first msg");
		reqList.add(reqObj);
		
		reqObj = new CustomRequest(-99, "Nine9", new Double(99.99), type, "This is Exception test for -ve Id");
		reqList.add(reqObj);
		
//		MessageConverter msgConverter = appCtx.getBean("defaultMessageConverter", MessageConverter.class);
		
		MessagingService msgSvc = appCtx.getBean("messagingService", MessagingService.class);
		for (CustomRequest req : reqList) {
			System.out.println(RabbitMQSpringConstants.DATE_FORMAT.format(new Date()) + "---Sending message: "+ req);
			msgSvc.sendMessageToRabbitMQ(req);
			Thread.sleep(1000);
		}
	}

}
