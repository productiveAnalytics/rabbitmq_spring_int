package com.productiveanalytics.rabbitmq_spring_int;

import java.util.Properties;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import com.productiveanalytics.rabbitmq_spring_int.config.BeanConfigurationsLoader;

public class IntegratorApp {

	public static void main(String[] args) {
		IntegratorApp app = new IntegratorApp();
		app.testSpringRabbitMQIntegration();
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
	
	private void testSpringRabbitMQIntegration()
	{
		BeanConfigurationsLoader loader = BeanConfigurationsLoader.getInstance();
		ApplicationContext appCtx = loader.getApplicationContext();
		rabbitMQProperties = appCtx.getBean("rabbitMQProperties", Properties.class);
		rabbitmqConnectionFactory = appCtx.getBean("rabbitmqConnectionFactory", ConnectionFactory.class);
		
		assert(rabbitMQProperties != null);
		System.out.println(rabbitMQProperties);
		
		
	}

}
