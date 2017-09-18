package com.productiveanalytics.rabbitmq_spring_int.service;

import com.productiveanalytics.rabbitmq_spring_int.model.CustomRequest;

public interface MessagingService
{
	public boolean sendMessageToRabbitMQ(CustomRequest requestObject);
}
