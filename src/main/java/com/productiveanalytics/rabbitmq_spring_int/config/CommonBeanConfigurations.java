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
		
		String rabbitPropertyFilename;
		
		if (RABBIT_PROPERTIES != null && RABBIT_PROPERTIES.trim().length() > 0) {
			rabbitPropertyFilename = RABBIT_PROPERTIES;
		} else {
			File defaultPropertyFile = new File(DEFAULT_PROPERTY_FILE_NAME); 
			System.out.println("Using default file: "+ defaultPropertyFile.getCanonicalPath());
			rabbitPropertyFilename = DEFAULT_PROPERTY_FILE_NAME;
		}
		
		
		InputStream inputStream = new FileInputStream(rabbitPropertyFilename);
		properties.load(inputStream);
		
		System.out.println(RabbitMQSpringConstants.PROP_HOST +"="+ properties.getProperty(RabbitMQSpringConstants.PROP_HOST));
		System.out.println(RabbitMQSpringConstants.PROP_VIRTUAL_HOST +"="+ properties.getProperty(RabbitMQSpringConstants.PROP_VIRTUAL_HOST));
		System.out.println(RabbitMQSpringConstants.PROP_USER_NAME +"="+ properties.getProperty(RabbitMQSpringConstants.PROP_USER_NAME));
		
		return properties;
	}
	
}
