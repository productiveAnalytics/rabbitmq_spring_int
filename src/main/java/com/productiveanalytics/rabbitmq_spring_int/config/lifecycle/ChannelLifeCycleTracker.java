package com.productiveanalytics.rabbitmq_spring_int.config.lifecycle;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.Channel;

/**
 * @author LChawathe
 */

public final class ChannelLifeCycleTracker 
{
	private static ChannelLifeCycleTracker tracker = null;
	
	private Map<Integer, Channel> map = new HashMap<Integer, Channel>();
	
	
	private ChannelLifeCycleTracker(){
		
	}
	
	public static void addChannel(Integer channelId, Channel channel){
		getInstance().map.put(channelId, channel);
	}
	
	public static void removeChannel(Integer channelId){
		getInstance().map.remove(channelId);
	}
	
	public static Collection<Channel> getAllChannels(){
		return getInstance().map.values();
	}
	
	public static Channel getChannel(Integer channelId){
		return getInstance().map.get(channelId);
	}
	
	public static ChannelLifeCycleTracker getInstance(){
		if(null == tracker){
			tracker = new ChannelLifeCycleTracker();
		}
		return tracker;
	}
}
