package com.productiveanalytics.rabbitmq_spring_int.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Programmatic configuration loader based on Annotations
 * 
 * @author LChawathe
 *
 */
public class BeanConfigurationsLoader
{
	private static BeanConfigurationsLoader loader;
	private static String BASE_PACKAGE = "com.productiveanalytics.rabbitmq_spring_int";
	
	private AnnotationConfigApplicationContext appContext;
	
	private BeanConfigurationsLoader(){
		/*
		 * Method 1 : Load Configuration class
		 */
//		this.appContext = new AnnotationConfigApplicationContext();
//		appContext.register(CommonBeanConfigurations.class);		// @Order(1)
//		appContext.register(RabbitMQBeanConfigurations.class);		// @Order(2)
//		appContext.refresh();
		
		/*
		 * Method 2 : Scan packages and load Configuration classes.
		 * 			  Equivalent of  <context:component-scan base-package="com.productiveanalytics.rabbitmq_spring_int" /> 
		 */
		this.appContext = new AnnotationConfigApplicationContext();
		appContext.scan(BASE_PACKAGE);
		appContext.refresh();
	}
	
	public static synchronized BeanConfigurationsLoader getInstance() {
		if (loader == null) {
			loader = new BeanConfigurationsLoader();
		}
		
		return loader;
	}
	
	public synchronized ApplicationContext getApplicationContext() {
		return getInstance().appContext;
	}
}
