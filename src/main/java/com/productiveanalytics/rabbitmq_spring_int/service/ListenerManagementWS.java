package com.productiveanalytics.rabbitmq_spring_int.service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import javax.jws.soap.SOAPBinding;

@WebService(targetNamespace="http://www.productiveAnalytics.com/RabbitMQ_Sprint_int")
@SOAPBinding(style=SOAPBinding.Style.RPC)
public interface ListenerManagementWS {

	@WebMethod
	@WebResult(name = "responseString")
	String changeListenerStatus(@WebParam(name="listener") String listenerName);
	
	@WebMethod
	@WebResult(name = "responseString")
	String getListenerStatus(@WebParam(name="listener") String listenerName);
}