package com.productiveanalytics.rabbitmq_spring_int.model;

import java.io.IOException;
import java.io.Serializable;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class CustomRequest implements Serializable
{
	private static final long serialVersionUID = -7399541075153688350L;
	private static final ObjectMapper CONVERTER = new ObjectMapper();
	
	private int id;
	private String key;
	private Object value;
	private String type;
	private String note;
	
	public CustomRequest() {
		// need this for Jackson
	}
	
	public CustomRequest(int id, String key, Object value, String type) {
		this(id, key, value, type, null);
	}
	
	public CustomRequest(int id, String key, Object value, String type, String note) {
		this.id = id;
		this.type = type;
		
		this.key = key;
		this.value = value;
		
		this.note = note;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}	

	@Override
	public String toString() {
		String jsonStr = null;
		try {
			jsonStr = CONVERTER.writeValueAsString(this);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonStr;
	}
	
	public static CustomRequest parse(String jsonContent) {
		try {
			CustomRequest readTestReq = CONVERTER.readValue(jsonContent, CustomRequest.class);
			return readTestReq;
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
//	public static void main(String[] args) {
//		CustomRequest writeTestReq = new CustomRequest (1, "one", "1.1.1", "first");
//		System.out.println(writeTestReq.toString());
//		
//		String readVal = "{'id':2,'key':'Two','value':'2.22.222','note':'Second'}".replaceAll("'", "\"");
//		System.out.println("Input="+ readVal);
//		CustomRequest readTestReq = parse(readVal);
//		System.out.println(readTestReq);
//	}
}
