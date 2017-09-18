package com.productiveanalytics.rabbitmq_spring_int.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;

import com.productiveanalytics.rabbitmq_spring_int.constants.RabbitMQSpringConstants;


/**
 * 
 * @author LChawathe
 *
 */

@Configuration
@Order(value=1)
public class CommonBeanConfigurations
			 implements InitializingBean
{
	private static final String RABBIT_PROPERTIES  = System.getProperty("rabbitmq.properties");
	
	private static final String DEFAULT_PROPERTY_FILE_NAME = "default_rabbit_mq.properties";
	
	public void afterPropertiesSet() throws Exception {
		System.err.println("CommonBeanConfigurations initialized >>>");
	}
	
	@Bean(name="rabbitMQProperties") 
	@Scope("singleton")
	public Properties getRabbitMQProperties() 
					  throws IOException
	{
		Properties properties = new Properties();
		
		InputStream inputStream;
		if (RABBIT_PROPERTIES != null && RABBIT_PROPERTIES.trim().length() > 0) {
			inputStream = new FileInputStream(RABBIT_PROPERTIES);
		} else {
			inputStream = getClass().getResourceAsStream("/"+ DEFAULT_PROPERTY_FILE_NAME);
		}
		
		Assert.notNull(inputStream, "InSteam must not be null here");
		properties.load(inputStream);
		
		System.out.println(RabbitMQSpringConstants.PROP_HOST +"="+ properties.getProperty(RabbitMQSpringConstants.PROP_HOST));
		System.out.println(RabbitMQSpringConstants.PROP_VIRTUAL_HOST +"="+ properties.getProperty(RabbitMQSpringConstants.PROP_VIRTUAL_HOST));
		System.out.println(RabbitMQSpringConstants.PROP_USER_NAME +"="+ properties.getProperty(RabbitMQSpringConstants.PROP_USER_NAME));
		
		return properties;
	}
	
}
